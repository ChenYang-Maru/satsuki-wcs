package io.renren.modules.plc.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.renren.common.constant.Constant;
import io.renren.common.page.PageData;
import io.renren.common.service.impl.BaseServiceImpl;
import io.renren.common.utils.ConvertUtils;
import io.renren.modules.plc.dao.PlcDeviceDao;
import io.renren.modules.plc.dto.PlcDeviceDTO;
import io.renren.modules.plc.entity.PlcDeviceEntity;
import io.renren.modules.plc.service.PlcDeviceService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Map;

/**
 * PLC设备管理服务实现
 */
@Service
@AllArgsConstructor
public class PlcDeviceServiceImpl extends BaseServiceImpl<PlcDeviceDao, PlcDeviceEntity>
        implements PlcDeviceService {

    @Override
    public PageData<PlcDeviceDTO> page(Map<String, Object> params) {
        IPage<PlcDeviceEntity> page = baseDao.selectPage(
                getPage(params, Constant.CREATE_DATE, false),
                getWrapper(params)
        );
        return getPageData(page, PlcDeviceDTO.class);
    }

    @Override
    public PlcDeviceDTO get(Long id) {
        PlcDeviceEntity entity = baseDao.selectById(id);
        return ConvertUtils.sourceToTarget(entity, PlcDeviceDTO.class);
    }

    private QueryWrapper<PlcDeviceEntity> getWrapper(Map<String, Object> params) {
        String deviceName = (String) params.get("deviceName");
        String protocol = (String) params.get("protocol");

        QueryWrapper<PlcDeviceEntity> wrapper = new QueryWrapper<>();
        wrapper.like(StrUtil.isNotBlank(deviceName), "device_name", deviceName);
        wrapper.eq(StrUtil.isNotBlank(protocol), "protocol", protocol);
        return wrapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void save(PlcDeviceDTO dto) {
        PlcDeviceEntity entity = ConvertUtils.sourceToTarget(dto, PlcDeviceEntity.class);
        if (entity.getStatus() == null) {
            entity.setStatus(1);
        }
        if (entity.getConnectTimeout() == null) {
            entity.setConnectTimeout(5000);
        }
        this.insert(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(PlcDeviceDTO dto) {
        PlcDeviceEntity entity = ConvertUtils.sourceToTarget(dto, PlcDeviceEntity.class);
        this.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteBatch(Long[] ids) {
        this.deleteBatchIds(Arrays.asList(ids));
    }
}
