# 基于工作流的高校实验室设备报修管理系统图集

本文档用于项目需求分析、概要设计、详细设计和答辩展示，包含 UML 用例图、业务活动图、典型时序图、类图、系统架构图和 E-R 图。

## 1. UML 用例图

```mermaid
flowchart LR
    Reporter["报修人<br/>学生/教师/设备使用者"]
    LabAdmin["实验室管理员"]
    Maintainer["维修人员"]
    SysAdmin["系统管理员"]

    subgraph System["高校实验室设备报修管理系统"]
        UC01(("登录/退出系统"))
        UC02(("查看设备信息"))
        UC03(("提交设备报修"))
        UC04(("查看报修进度"))
        UC05(("验收维修结果"))
        UC06(("审核报修单"))
        UC07(("派发维修工单"))
        UC08(("关闭无效工单"))
        UC09(("查看维修任务"))
        UC10(("更新维修状态"))
        UC11(("填写维修结果"))
        UC12(("用户与角色管理"))
        UC13(("实验室管理"))
        UC14(("设备档案管理"))
        UC15(("查询统计报表"))
        UC16(("维护状态字典"))
    end

    Reporter --> UC01
    Reporter --> UC02
    Reporter --> UC03
    Reporter --> UC04
    Reporter --> UC05

    LabAdmin --> UC01
    LabAdmin --> UC06
    LabAdmin --> UC07
    LabAdmin --> UC08
    LabAdmin --> UC14
    LabAdmin --> UC15

    Maintainer --> UC01
    Maintainer --> UC09
    Maintainer --> UC10
    Maintainer --> UC11

    SysAdmin --> UC01
    SysAdmin --> UC12
    SysAdmin --> UC13
    SysAdmin --> UC14
    SysAdmin --> UC16
    SysAdmin --> UC15
```

## 2. 报修业务活动图

```mermaid
flowchart TD
    Start([开始])
    Login["用户登录系统"]
    SelectEquipment["选择故障设备"]
    SubmitOrder["填写故障描述、位置、紧急程度并提交报修"]
    PendingReview["工单状态：待审核"]
    Review{"管理员审核是否有效？"}
    ReturnOrder["退回补充或关闭无效工单"]
    Assign["分配维修人员"]
    Assigned["工单状态：已派单"]
    AcceptTask["维修人员接单"]
    Repairing["维修处理并记录故障原因、处理措施"]
    SubmitResult["提交维修结果"]
    PendingAccept["工单状态：待验收"]
    Accept{"验收是否通过？"}
    Rework["退回维修处理"]
    Closed["工单关闭并归档"]
    End([结束])

    Start --> Login --> SelectEquipment --> SubmitOrder --> PendingReview --> Review
    Review -- 否 --> ReturnOrder --> End
    Review -- 是 --> Assign --> Assigned --> AcceptTask --> Repairing --> SubmitResult --> PendingAccept --> Accept
    Accept -- 否 --> Rework --> Repairing
    Accept -- 是 --> Closed --> End
```

## 3. 典型时序图：提交报修到验收关闭

```mermaid
sequenceDiagram
    actor Reporter as 报修人
    participant Web as Vue前端
    participant Auth as Spring Security
    participant RepairApi as 报修工单Controller
    participant RepairService as 报修业务Service
    participant DB as MySQL数据库
    actor Admin as 实验室管理员
    actor Maintainer as 维修人员

    Reporter->>Web: 登录并填写报修单
    Web->>Auth: 提交登录凭据
    Auth-->>Web: 返回登录结果和角色信息
    Web->>RepairApi: POST /api/repair-orders
    RepairApi->>RepairService: createRepairOrder(dto)
    RepairService->>DB: 写入报修单，状态=待审核
    DB-->>RepairService: 返回工单ID
    RepairService-->>RepairApi: 创建成功
    RepairApi-->>Web: 返回工单详情
    Web-->>Reporter: 显示待审核

    Admin->>Web: 审核并派单
    Web->>RepairApi: PUT /api/repair-orders/{id}/assign
    RepairApi->>RepairService: assign(orderId, maintainerId)
    RepairService->>DB: 更新状态=已派单，写入维修人员
    DB-->>RepairService: 更新成功

    Maintainer->>Web: 查看维修任务并处理
    Web->>RepairApi: PUT /api/repair-orders/{id}/repair
    RepairApi->>RepairService: submitRepairResult(orderId, result)
    RepairService->>DB: 写入维修记录，状态=待验收

    Reporter->>Web: 验收维修结果
    Web->>RepairApi: PUT /api/repair-orders/{id}/accept
    RepairApi->>RepairService: accept(orderId, accepted)
    RepairService->>DB: 写入验收记录，状态=已关闭
    DB-->>RepairService: 保存成功
    RepairService-->>RepairApi: 验收完成
    RepairApi-->>Web: 返回关闭结果
    Web-->>Reporter: 显示工单已归档
```

## 4. UML 类图

```mermaid
classDiagram
    class User {
        +Long id
        +String username
        +String password
        +String realName
        +String phone
        +String email
        +UserStatus status
        +LocalDateTime createdAt
    }

    class Role {
        +Long id
        +String roleCode
        +String roleName
        +String description
    }

    class Lab {
        +Long id
        +String labName
        +String location
        +Long managerId
        +String contactPhone
    }

    class Equipment {
        +Long id
        +String equipmentNo
        +String equipmentName
        +String category
        +Long labId
        +Long ownerId
        +EquipmentStatus status
        +LocalDate purchaseDate
    }

    class RepairOrder {
        +Long id
        +String orderNo
        +Long equipmentId
        +Long reporterId
        +Long managerId
        +Long maintainerId
        +String faultDescription
        +String location
        +UrgencyLevel urgency
        +RepairStatus status
        +LocalDateTime submittedAt
        +LocalDateTime closedAt
    }

    class MaintenanceRecord {
        +Long id
        +Long repairOrderId
        +Long maintainerId
        +String faultReason
        +String repairAction
        +String repairResult
        +LocalDateTime startedAt
        +LocalDateTime finishedAt
    }

    class AcceptanceRecord {
        +Long id
        +Long repairOrderId
        +Long accepterId
        +Boolean accepted
        +String acceptanceOpinion
        +LocalDateTime acceptedAt
    }

    class OperationLog {
        +Long id
        +Long operatorId
        +Long repairOrderId
        +String action
        +String remark
        +LocalDateTime operatedAt
    }

    User "many" -- "many" Role : 拥有
    Lab "1" -- "many" Equipment : 管理
    User "1" -- "many" Lab : 负责
    Equipment "1" -- "many" RepairOrder : 被报修
    User "1" -- "many" RepairOrder : 提交
    User "1" -- "many" RepairOrder : 处理/派单
    RepairOrder "1" -- "many" MaintenanceRecord : 维修记录
    RepairOrder "1" -- "many" AcceptanceRecord : 验收记录
    RepairOrder "1" -- "many" OperationLog : 操作日志
    User "1" -- "many" OperationLog : 执行
```

## 5. 系统架构图

```mermaid
flowchart TB
    subgraph Client["客户端层"]
        Browser["浏览器<br/>学生/教师/管理员/维修人员"]
    end

    subgraph Frontend["前端展示层：Vue 3 + Vite"]
        LoginPage["登录页面"]
        RepairPage["报修申请页面"]
        OrderPage["工单列表/详情页面"]
        AdminPage["设备与用户管理页面"]
        StatPage["查询统计页面"]
        Router["Vue Router / 页面路由"]
        ApiClient["Axios/Fetch API 调用"]
    end

    subgraph Backend["后端服务层：Spring Boot"]
        Security["Spring Security<br/>认证与角色权限控制"]
        AuthController["认证Controller"]
        UserController["用户/角色Controller"]
        EquipmentController["设备Controller"]
        RepairController["报修工单Controller"]
        ReportController["统计Controller"]

        AuthService["认证服务"]
        UserService["用户角色服务"]
        EquipmentService["设备档案服务"]
        RepairService["报修工作流服务"]
        ReportService["统计分析服务"]

        JdbcRepo["JDBC Repository<br/>数据访问层"]
    end

    subgraph Data["数据存储层：MySQL"]
        UserTable[("user / role / user_role")]
        LabTable[("lab")]
        EquipmentTable[("equipment")]
        RepairTable[("repair_order")]
        RecordTable[("maintenance_record / acceptance_record")]
        LogTable[("operation_log")]
    end

    Browser --> LoginPage
    Browser --> RepairPage
    Browser --> OrderPage
    Browser --> AdminPage
    Browser --> StatPage
    LoginPage --> Router
    RepairPage --> Router
    OrderPage --> Router
    AdminPage --> Router
    StatPage --> Router
    Router --> ApiClient
    ApiClient -->|HTTP/JSON REST API| Security

    Security --> AuthController
    Security --> UserController
    Security --> EquipmentController
    Security --> RepairController
    Security --> ReportController

    AuthController --> AuthService
    UserController --> UserService
    EquipmentController --> EquipmentService
    RepairController --> RepairService
    ReportController --> ReportService

    AuthService --> JdbcRepo
    UserService --> JdbcRepo
    EquipmentService --> JdbcRepo
    RepairService --> JdbcRepo
    ReportService --> JdbcRepo

    JdbcRepo --> UserTable
    JdbcRepo --> LabTable
    JdbcRepo --> EquipmentTable
    JdbcRepo --> RepairTable
    JdbcRepo --> RecordTable
    JdbcRepo --> LogTable
```

## 6. E-R 图

```mermaid
erDiagram
    USER {
        bigint id PK
        varchar username UK
        varchar password
        varchar real_name
        varchar phone
        varchar email
        varchar status
        datetime created_at
        datetime updated_at
    }

    ROLE {
        bigint id PK
        varchar role_code UK
        varchar role_name
        varchar description
    }

    USER_ROLE {
        bigint user_id FK
        bigint role_id FK
    }

    LAB {
        bigint id PK
        varchar lab_name
        varchar location
        bigint manager_id FK
        varchar contact_phone
        datetime created_at
    }

    EQUIPMENT {
        bigint id PK
        varchar equipment_no UK
        varchar equipment_name
        varchar category
        bigint lab_id FK
        bigint owner_id FK
        varchar status
        date purchase_date
        datetime created_at
    }

    REPAIR_ORDER {
        bigint id PK
        varchar order_no UK
        bigint equipment_id FK
        bigint reporter_id FK
        bigint manager_id FK
        bigint maintainer_id FK
        varchar fault_description
        varchar fault_location
        varchar urgency
        varchar status
        datetime submitted_at
        datetime assigned_at
        datetime closed_at
    }

    MAINTENANCE_RECORD {
        bigint id PK
        bigint repair_order_id FK
        bigint maintainer_id FK
        varchar fault_reason
        varchar repair_action
        varchar repair_result
        datetime started_at
        datetime finished_at
    }

    ACCEPTANCE_RECORD {
        bigint id PK
        bigint repair_order_id FK
        bigint accepter_id FK
        boolean accepted
        varchar acceptance_opinion
        datetime accepted_at
    }

    OPERATION_LOG {
        bigint id PK
        bigint operator_id FK
        bigint repair_order_id FK
        varchar action
        varchar remark
        datetime operated_at
    }

    USER ||--o{ USER_ROLE : has
    ROLE ||--o{ USER_ROLE : assigned
    USER ||--o{ LAB : manages
    LAB ||--o{ EQUIPMENT : contains
    USER ||--o{ EQUIPMENT : owns
    EQUIPMENT ||--o{ REPAIR_ORDER : has_fault
    USER ||--o{ REPAIR_ORDER : reports
    USER ||--o{ REPAIR_ORDER : maintains
    USER ||--o{ REPAIR_ORDER : manages_order
    REPAIR_ORDER ||--o{ MAINTENANCE_RECORD : has
    USER ||--o{ MAINTENANCE_RECORD : writes
    REPAIR_ORDER ||--o{ ACCEPTANCE_RECORD : has
    USER ||--o{ ACCEPTANCE_RECORD : accepts
    REPAIR_ORDER ||--o{ OPERATION_LOG : records
    USER ||--o{ OPERATION_LOG : operates
```

## 7. 数据表设计说明

| 表名 | 说明 |
|---|---|
| user | 保存系统用户，包括报修人、管理员、维修人员和系统管理员。 |
| role | 保存角色定义，如 REPORTER、LAB_ADMIN、MAINTAINER、SYS_ADMIN。 |
| user_role | 用户与角色多对多关联表。 |
| lab | 保存实验室基础信息及负责人。 |
| equipment | 保存实验室设备档案和当前状态。 |
| repair_order | 保存报修工单主表，记录当前状态和关键人员。 |
| maintenance_record | 保存维修处理过程、故障原因、处理措施和维修结果。 |
| acceptance_record | 保存验收结果、验收意见和验收时间。 |
| operation_log | 保存工单流转过程中的关键操作记录。 |

## 8. 工单状态建议

| 状态编码 | 状态名称 | 说明 |
|---|---|---|
| SUBMITTED | 待审核 | 报修人已提交，等待管理员审核。 |
| RETURNED | 已退回 | 管理员认为信息不足，退回报修人补充。 |
| ASSIGNED | 已派单 | 管理员已分配维修人员。 |
| REPAIRING | 维修中 | 维修人员已接单并处理中。 |
| WAIT_ACCEPTANCE | 待验收 | 维修人员已提交维修结果，等待验收。 |
| REWORK | 返修中 | 验收不通过，退回继续维修。 |
| CLOSED | 已关闭 | 验收通过并归档。 |
| CANCELED | 已取消 | 无效报修或人工取消。 |
