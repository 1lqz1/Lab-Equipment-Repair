package org.example.lab_equipment_repair.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.example.lab_equipment_repair.common.BusinessException;
import org.example.lab_equipment_repair.dto.SysDictItemRequest;
import org.example.lab_equipment_repair.dto.SysDictRequest;
import org.example.lab_equipment_repair.entity.SysDict;
import org.example.lab_equipment_repair.entity.SysDictItem;
import org.example.lab_equipment_repair.mapper.SysDictItemMapper;
import org.example.lab_equipment_repair.mapper.SysDictMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SysDictService {

    private final SysDictMapper sysDictMapper;
    private final SysDictItemMapper sysDictItemMapper;

    public List<SysDict> listDicts(String keyword) {
        return sysDictMapper.selectList(new LambdaQueryWrapper<SysDict>()
                .and(keyword != null && !keyword.isBlank(), query -> query
                        .like(SysDict::getDictCode, keyword)
                        .or()
                        .like(SysDict::getDictName, keyword))
                .orderByAsc(SysDict::getSortOrder)
                .orderByDesc(SysDict::getCreatedAt));
    }

    public SysDict createDict(SysDictRequest request) {
        ensureDictCodeAvailable(null, request.getDictCode());
        SysDict dict = new SysDict();
        fillDict(dict, request);
        sysDictMapper.insert(dict);
        return dict;
    }

    public SysDict updateDict(Long id, SysDictRequest request) {
        SysDict dict = getDictById(id);
        ensureDictCodeAvailable(id, request.getDictCode());
        fillDict(dict, request);
        sysDictMapper.updateById(dict);
        return dict;
    }

    @Transactional
    public void deleteDict(Long id) {
        getDictById(id);
        sysDictItemMapper.delete(new LambdaQueryWrapper<SysDictItem>().eq(SysDictItem::getDictId, id));
        sysDictMapper.deleteById(id);
    }

    public List<SysDictItem> listItems(String dictCode, Boolean enabledOnly) {
        SysDict dict = getDictByCode(dictCode);
        return sysDictItemMapper.selectList(new LambdaQueryWrapper<SysDictItem>()
                .eq(SysDictItem::getDictId, dict.getId())
                .eq(Boolean.TRUE.equals(enabledOnly), SysDictItem::getEnabled, true)
                .orderByAsc(SysDictItem::getSortOrder)
                .orderByAsc(SysDictItem::getId));
    }

    public SysDictItem createItem(Long dictId, SysDictItemRequest request) {
        getDictById(dictId);
        ensureItemValueAvailable(dictId, null, request.getItemValue());
        SysDictItem item = new SysDictItem();
        fillItem(item, dictId, request);
        sysDictItemMapper.insert(item);
        return item;
    }

    public SysDictItem updateItem(Long itemId, SysDictItemRequest request) {
        SysDictItem item = getItemById(itemId);
        ensureItemValueAvailable(item.getDictId(), itemId, request.getItemValue());
        fillItem(item, item.getDictId(), request);
        sysDictItemMapper.updateById(item);
        return item;
    }

    public void deleteItem(Long itemId) {
        getItemById(itemId);
        sysDictItemMapper.deleteById(itemId);
    }

    private void fillDict(SysDict dict, SysDictRequest request) {
        dict.setDictCode(request.getDictCode().trim());
        dict.setDictName(request.getDictName().trim());
        dict.setEnabled(request.getEnabled());
        dict.setSortOrder(request.getSortOrder() == null ? 0 : request.getSortOrder());
        dict.setRemark(normalize(request.getRemark()));
    }

    private void fillItem(SysDictItem item, Long dictId, SysDictItemRequest request) {
        item.setDictId(dictId);
        item.setItemValue(request.getItemValue().trim());
        item.setItemLabel(request.getItemLabel().trim());
        item.setEnabled(request.getEnabled());
        item.setSortOrder(request.getSortOrder() == null ? 0 : request.getSortOrder());
        item.setRemark(normalize(request.getRemark()));
    }

    private SysDict getDictById(Long id) {
        SysDict dict = sysDictMapper.selectById(id);
        if (dict == null) {
            throw new BusinessException("字典不存在");
        }
        return dict;
    }

    private SysDict getDictByCode(String dictCode) {
        SysDict dict = sysDictMapper.selectOne(new LambdaQueryWrapper<SysDict>()
                .eq(SysDict::getDictCode, dictCode)
                .eq(SysDict::getEnabled, true));
        if (dict == null) {
            throw new BusinessException("字典不存在或已停用");
        }
        return dict;
    }

    private SysDictItem getItemById(Long itemId) {
        SysDictItem item = sysDictItemMapper.selectById(itemId);
        if (item == null) {
            throw new BusinessException("字典项不存在");
        }
        return item;
    }

    private void ensureDictCodeAvailable(Long currentId, String dictCode) {
        SysDict exists = sysDictMapper.selectOne(new LambdaQueryWrapper<SysDict>()
                .eq(SysDict::getDictCode, dictCode.trim()));
        if (exists != null && !exists.getId().equals(currentId)) {
            throw new BusinessException("字典编码已存在");
        }
    }

    private void ensureItemValueAvailable(Long dictId, Long currentItemId, String itemValue) {
        SysDictItem exists = sysDictItemMapper.selectOne(new LambdaQueryWrapper<SysDictItem>()
                .eq(SysDictItem::getDictId, dictId)
                .eq(SysDictItem::getItemValue, itemValue.trim()));
        if (exists != null && !exists.getId().equals(currentItemId)) {
            throw new BusinessException("字典项值已存在");
        }
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
