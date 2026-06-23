#!/usr/bin/env python3
"""
实验室设备报修管理系统接口冒烟测试。

运行前要求：
1. MySQL 已执行 docs/alter_user_registration_profile.sql。
2. 后端已启动，默认地址 http://127.0.0.1:18080/api。
3. DataInitializer 已创建 admin / 123456。

示例：
python tests/api_smoke_test.py
python tests/api_smoke_test.py --base-url http://localhost:18080/api --admin admin --password 123456
"""

from __future__ import annotations

import argparse
import base64
import json
import random
import sys
import time
import urllib.error
import urllib.parse
import urllib.request
from dataclasses import dataclass
from typing import Any


PNG_1X1 = base64.b64decode(
    "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+/p9sAAAAASUVORK5CYII="
)


@dataclass
class ApiResult:
    status: int
    body: dict[str, Any] | str


class ApiClient:
    """仅使用 Python 标准库封装 HTTP 请求，避免额外安装测试依赖。"""

    def __init__(self, base_url: str) -> None:
        self.base_url = base_url.rstrip("/")
        self.token: str | None = None

    def set_token(self, token: str | None) -> None:
        self.token = token

    def get(self, path: str) -> ApiResult:
        return self._request("GET", path)

    def post_json(self, path: str, payload: dict[str, Any]) -> ApiResult:
        return self._request("POST", path, payload)

    def put_json(self, path: str, payload: dict[str, Any] | None = None) -> ApiResult:
        return self._request("PUT", path, payload)

    def delete(self, path: str) -> ApiResult:
        return self._request("DELETE", path)

    def post_multipart_file(self, path: str, field_name: str, filename: str, content_type: str, data: bytes) -> ApiResult:
        # 后端头像接口使用 multipart/form-data，这里手工拼装请求体以保持脚本零依赖。
        boundary = f"----LabRepairBoundary{int(time.time() * 1000)}"
        body = (
            f"--{boundary}\r\n"
            f'Content-Disposition: form-data; name="{field_name}"; filename="{filename}"\r\n'
            f"Content-Type: {content_type}\r\n\r\n"
        ).encode("utf-8") + data + f"\r\n--{boundary}--\r\n".encode("utf-8")
        return self._request(
            "POST",
            path,
            raw_body=body,
            content_type=f"multipart/form-data; boundary={boundary}",
        )

    def _request(
        self,
        method: str,
        path: str,
        payload: dict[str, Any] | None = None,
        raw_body: bytes | None = None,
        content_type: str = "application/json",
    ) -> ApiResult:
        url = f"{self.base_url}{path}"
        headers = {"Accept": "application/json"}
        data = raw_body
        if payload is not None:
            data = json.dumps(payload).encode("utf-8")
            headers["Content-Type"] = content_type
        elif raw_body is not None:
            headers["Content-Type"] = content_type
        if self.token:
            headers["Authorization"] = f"Bearer {self.token}"

        # HTTPError 也会携带后端返回体，测试需要读取其中的 code/message。
        request = urllib.request.Request(url, data=data, headers=headers, method=method)
        try:
            with urllib.request.urlopen(request, timeout=10) as response:
                return ApiResult(response.status, self._decode_body(response.read()))
        except urllib.error.HTTPError as error:
            return ApiResult(error.code, self._decode_body(error.read()))
        except urllib.error.URLError as error:
            raise RuntimeError(f"无法连接后端服务：{url}，原因：{error.reason}") from error

    @staticmethod
    def _decode_body(raw: bytes) -> dict[str, Any] | str:
        text = raw.decode("utf-8", errors="replace")
        if not text:
            return {}
        try:
            return json.loads(text)
        except json.JSONDecodeError:
            return text


class TestRunner:
    """按业务流程组织测试，前一个步骤创建的数据会被后续步骤复用。"""

    def __init__(self, client: ApiClient, admin: str, password: str) -> None:
        self.client = client
        self.admin = admin
        self.password = password
        self.failures: list[str] = []
        # 使用时间戳和随机数生成测试账号，降低重复运行时的账号冲突概率。
        suffix = f"{int(time.time())}_{random.randint(1000, 9999)}"
        self.pending_user = f"pending_{suffix}"
        self.created_user = f"created_{suffix}"
        self.lab_name = f"横向扩展测试实验室_{suffix}"
        self.equipment_code = f"EQ-X-{suffix}"
        self.order_id: int | None = None

    def run(self) -> int:
        # 顺序执行很重要：注册用户、审批用户、禁用用户等测试存在数据依赖。
        tests = [
            self.test_admin_login_success,
            self.test_login_wrong_password,
            self.test_public_register_pending_user,
            self.test_pending_user_cannot_login,
            self.test_admin_create_user,
            self.test_admin_user_status_actions,
            self.test_dict_items_available,
            self.test_lab_crud_actions,
            self.test_equipment_crud_actions,
            self.test_order_horizontal_actions,
            self.test_admin_update_user_and_reset_password,
            self.test_profile_update,
            self.test_avatar_upload,
        ]
        for test in tests:
            name = test.__name__.replace("test_", "")
            try:
                test()
                print(f"[PASS] {name}")
            except AssertionError as error:
                self.failures.append(f"{name}: {error}")
                print(f"[FAIL] {name}: {error}")
            except RuntimeError as error:
                self.failures.append(f"{name}: {error}")
                print(f"[ERROR] {name}: {error}")

        if self.failures:
            print("\n失败项：")
            for failure in self.failures:
                print(f"- {failure}")
            return 1
        print("\n全部接口冒烟测试通过")
        return 0

    def test_admin_login_success(self) -> None:
        """验证基础管理员账号可登录，并保存 token 供后续受保护接口使用。"""
        result = self.client.post_json("/auth/login", {"username": self.admin, "password": self.password})
        self.assert_api_success(result, "管理员登录失败")
        token = result.body["data"]["token"]  # type: ignore[index]
        assert token, "登录响应缺少 token"
        self.client.set_token(token)

    def test_login_wrong_password(self) -> None:
        """验证密码错误时后端返回明确认证错误，而不是前端兜底的繁忙提示。"""
        result = self.client.post_json("/auth/login", {"username": self.admin, "password": "wrong-password"})
        self.assert_api_fail(result, expected_status=401, expected_code=401, expected_message="账号或密码错误")

    def test_public_register_pending_user(self) -> None:
        """验证普通注册不需要登录，且注册结果固定为 REPORTER/PENDING。"""
        self.client.set_token(None)
        result = self.client.post_json(
            "/auth/register",
            {
                "username": self.pending_user,
                "password": "123456",
                "realName": "待审核测试用户",
                "phone": "13800000000",
            },
        )
        self.assert_api_success(result, "公开注册失败")
        data = result.body["data"]  # type: ignore[index]
        assert data["role"] == "REPORTER", f"普通注册角色应为 REPORTER，实际为 {data['role']}"
        assert data["status"] == "PENDING", f"普通注册状态应为 PENDING，实际为 {data['status']}"

    def test_pending_user_cannot_login(self) -> None:
        """验证待审核用户不能登录，且返回待审核提示。"""
        result = self.client.post_json("/auth/login", {"username": self.pending_user, "password": "123456"})
        self.assert_api_fail(result, expected_status=403, expected_code=403, expected_message="账号待管理员审核")

    def test_admin_create_user(self) -> None:
        """验证管理员创建用户默认启用，并允许指定四类角色之一。"""
        self.login_admin()
        result = self.client.post_json(
            "/users",
            {
                "username": self.created_user,
                "password": "123456",
                "realName": "管理员创建测试用户",
                "phone": "13900000000",
                "role": "REPAIRER",
            },
        )
        self.assert_api_success(result, "管理员创建用户失败")
        data = result.body["data"]  # type: ignore[index]
        assert data["role"] == "REPAIRER", f"创建用户角色应为 REPAIRER，实际为 {data['role']}"
        assert data["status"] == "ACTIVE", f"管理员创建用户状态应为 ACTIVE，实际为 {data['status']}"

    def test_admin_user_status_actions(self) -> None:
        """验证审批、禁用、启用的状态流转和登录效果。"""
        self.login_admin()
        pending_id = self.find_user_id(self.pending_user)
        self.assert_api_success(self.client.put_json(f"/users/{pending_id}/approve"), "审批用户失败")
        self.login_as(self.pending_user, "123456")

        self.login_admin()
        created_id = self.find_user_id(self.created_user)
        self.assert_api_success(self.client.put_json(f"/users/{created_id}/disable"), "禁用用户失败")
        result = self.client.post_json("/auth/login", {"username": self.created_user, "password": "123456"})
        self.assert_api_fail(result, expected_status=403, expected_code=403, expected_message="账号已被禁用")

        self.login_admin()
        self.assert_api_success(self.client.put_json(f"/users/{created_id}/enable"), "启用用户失败")
        self.login_as(self.created_user, "123456")

    def test_dict_items_available(self) -> None:
        """验证第 8 项基础字典接口可读取常用下拉选项。"""
        self.login_admin()
        for dict_code in ("user_role", "user_status", "equipment_status", "order_status", "urgency_level", "equipment_category"):
            result = self.client.get(f"/dicts/{dict_code}/items")
            self.assert_api_success(result, f"读取字典 {dict_code} 失败")
            assert result.body["data"], f"字典 {dict_code} 没有任何字典项"  # type: ignore[index]

    def test_lab_crud_actions(self) -> None:
        """验证实验室横向管理：新增、查询、编辑、停用、启用。"""
        self.login_admin()
        result = self.client.post_json(
            "/labs",
            {
                "name": self.lab_name,
                "location": "测试楼 501",
                "managerId": None,
                "description": "接口测试创建",
            },
        )
        self.assert_api_success(result, "新增实验室失败")
        lab = result.body["data"]  # type: ignore[index]
        assert lab["status"] == "ACTIVE", f"新增实验室应默认启用，实际 {lab['status']}"

        lab_id = int(lab["id"])
        self.assert_api_success(
            self.client.put_json(
                f"/labs/{lab_id}",
                {
                    "name": self.lab_name,
                    "location": "测试楼 502",
                    "managerId": None,
                    "description": "接口测试更新",
                },
            ),
            "编辑实验室失败",
        )
        self.assert_api_success(self.client.put_json(f"/labs/{lab_id}/disable"), "停用实验室失败")
        self.assert_api_success(self.client.put_json(f"/labs/{lab_id}/enable"), "启用实验室失败")

    def test_equipment_crud_actions(self) -> None:
        """验证设备横向管理：新增、查询、编辑、状态切换。"""
        self.login_admin()
        lab_id = self.find_lab_id(self.lab_name)
        result = self.client.post_json(
            "/equipment",
            {
                "code": self.equipment_code,
                "name": "横向扩展测试设备",
                "category": "电子测试",
                "labId": lab_id,
                "status": "NORMAL",
                "responsibleUserId": None,
                "purchaseDate": "2026-01-01",
            },
        )
        self.assert_api_success(result, "新增设备失败")
        equipment = result.body["data"]  # type: ignore[index]
        equipment_id = int(equipment["id"])

        self.assert_api_success(
            self.client.put_json(
                f"/equipment/{equipment_id}",
                {
                    "code": self.equipment_code,
                    "name": "横向扩展测试设备-更新",
                    "category": "教学设备",
                    "labId": lab_id,
                    "status": "NORMAL",
                    "responsibleUserId": None,
                    "purchaseDate": "2026-01-02",
                },
            ),
            "编辑设备失败",
        )
        disabled = self.client.put_json(f"/equipment/{equipment_id}/status", {"status": "DISABLED"})
        self.assert_api_success(disabled, "停用设备失败")
        normal = self.client.put_json(f"/equipment/{equipment_id}/status", {"status": "NORMAL"})
        self.assert_api_success(normal, "恢复设备失败")

    def test_order_horizontal_actions(self) -> None:
        """验证工单横向操作：提交、派单、转派、备注、取消。"""
        self.login_as(self.pending_user, "123456")
        equipment_id = self.find_equipment_id(self.equipment_code)
        result = self.client.post_json(
            "/repair-orders",
            {
                "equipmentId": equipment_id,
                "faultDescription": "横向操作测试故障",
                "urgency": "HIGH",
                "location": "测试楼 502",
                "contact": "13800000000",
            },
        )
        self.assert_api_success(result, "提交工单失败")
        self.order_id = int(result.body["data"]["id"])  # type: ignore[index]

        self.login_admin()
        repairer_id = self.find_user_id(self.created_user)
        self.assert_api_success(
            self.client.put_json(f"/repair-orders/{self.order_id}/assign", {"repairerId": repairer_id, "remark": "测试派单"}),
            "派单失败",
        )
        self.assert_api_success(
            self.client.put_json(f"/repair-orders/{self.order_id}/transfer", {"repairerId": repairer_id, "remark": "测试转派"}),
            "转派失败",
        )
        self.assert_api_success(
            self.client.post_json(f"/repair-orders/{self.order_id}/remarks", {"remark": "测试追加备注"}),
            "追加备注失败",
        )
        self.assert_api_success(
            self.client.put_json(f"/repair-orders/{self.order_id}/cancel", {"remark": "测试取消"}),
            "取消工单失败",
        )

    def test_admin_update_user_and_reset_password(self) -> None:
        """验证用户管理扩展：编辑用户资料、角色状态和重置密码。"""
        self.login_admin()
        created_id = self.find_user_id(self.created_user)
        self.assert_api_success(
            self.client.put_json(
                f"/users/{created_id}",
                {
                    "realName": "维修人员编辑测试",
                    "phone": "13600000000",
                    "role": "REPAIRER",
                    "status": "ACTIVE",
                },
            ),
            "编辑用户失败",
        )
        self.assert_api_success(
            self.client.put_json(f"/users/{created_id}/password", {"password": "654321"}),
            "重置密码失败",
        )
        self.login_as(self.created_user, "654321")

    def test_profile_update(self) -> None:
        """验证当前登录用户可以修改自己的姓名和电话。"""
        self.login_as(self.created_user, "654321")
        result = self.client.put_json("/profile", {"realName": "资料修改测试", "phone": "13700000000"})
        self.assert_api_success(result, "修改个人资料失败")
        data = result.body["data"]  # type: ignore[index]
        assert data["realName"] == "资料修改测试", "个人资料 realName 未更新"
        assert data["phone"] == "13700000000", "个人资料 phone 未更新"

    def test_avatar_upload(self) -> None:
        """验证头像接口接收图片文件，并返回可访问的相对路径。"""
        self.login_as(self.created_user, "654321")
        result = self.client.post_multipart_file("/profile/avatar", "file", "avatar.png", "image/png", PNG_1X1)
        self.assert_api_success(result, "头像上传失败")
        data = result.body["data"]  # type: ignore[index]
        avatar_path = data.get("avatarPath")
        assert avatar_path and avatar_path.startswith("/uploads/avatars/"), f"头像路径异常：{avatar_path}"

    def login_admin(self) -> None:
        self.login_as(self.admin, self.password)

    def login_as(self, username: str, password: str) -> None:
        # 每次登录前清空旧 token，避免前一个用户的身份污染当前请求。
        self.client.set_token(None)
        result = self.client.post_json("/auth/login", {"username": username, "password": password})
        self.assert_api_success(result, f"{username} 登录失败")
        self.client.set_token(result.body["data"]["token"])  # type: ignore[index]

    def find_user_id(self, username: str) -> int:
        # 用户状态接口按 id 操作，因此测试需要先从列表中定位临时账号。
        result = self.client.get("/users")
        self.assert_api_success(result, "查询用户列表失败")
        for user in result.body["data"]:  # type: ignore[index]
            if user["username"] == username:
                return int(user["id"])
        raise AssertionError(f"用户列表中找不到账号 {username}")

    def find_lab_id(self, lab_name: str) -> int:
        result = self.client.get(f"/labs?keyword={urllib.parse.quote(lab_name)}")
        self.assert_api_success(result, "查询实验室列表失败")
        for lab in result.body["data"]:  # type: ignore[index]
            if lab["name"] == lab_name:
                return int(lab["id"])
        raise AssertionError(f"实验室列表中找不到 {lab_name}")

    def find_equipment_id(self, code: str) -> int:
        result = self.client.get(f"/equipment?keyword={urllib.parse.quote(code)}")
        self.assert_api_success(result, "查询设备列表失败")
        for equipment in result.body["data"]:  # type: ignore[index]
            if equipment["code"] == code:
                return int(equipment["id"])
        raise AssertionError(f"设备列表中找不到 {code}")

    @staticmethod
    def assert_api_success(result: ApiResult, message: str) -> None:
        # 项目统一响应格式为 {code, message, data}，HTTP 和业务 code 都要检查。
        assert result.status == 200, f"{message}，HTTP {result.status}，响应 {result.body}"
        assert isinstance(result.body, dict), f"{message}，响应不是 JSON：{result.body}"
        assert result.body.get("code") == 200, f"{message}，业务 code={result.body.get('code')}，响应 {result.body}"

    @staticmethod
    def assert_api_fail(result: ApiResult, expected_status: int, expected_code: int, expected_message: str) -> None:
        # 失败断言同时检查 HTTP 状态、业务 code 和中文错误消息。
        assert result.status == expected_status, f"HTTP 状态应为 {expected_status}，实际 {result.status}，响应 {result.body}"
        assert isinstance(result.body, dict), f"失败响应不是 JSON：{result.body}"
        assert result.body.get("code") == expected_code, f"业务 code 应为 {expected_code}，实际 {result.body.get('code')}"
        assert expected_message in str(result.body.get("message")), (
            f"错误消息应包含 {expected_message}，实际 {result.body.get('message')}"
        )


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="实验室设备报修管理系统接口冒烟测试")
    parser.add_argument("--base-url", default="http://127.0.0.1:18080/api", help="后端 API 基础地址")
    parser.add_argument("--admin", default="admin", help="管理员账号")
    parser.add_argument("--password", default="123456", help="管理员密码")
    return parser.parse_args()


def main() -> int:
    args = parse_args()
    client = ApiClient(args.base_url)
    runner = TestRunner(client, args.admin, args.password)
    return runner.run()


if __name__ == "__main__":
    sys.exit(main())
