package io.renren.modules.plc.controller;

import io.renren.common.annotation.LogOperation;
import io.renren.common.constant.Constant;
import io.renren.common.page.PageData;
import io.renren.common.utils.Result;
import io.renren.common.validator.ValidatorUtils;
import io.renren.common.validator.group.AddGroup;
import io.renren.common.validator.group.DefaultGroup;
import io.renren.common.validator.group.UpdateGroup;
import io.renren.modules.plc.dto.PlcDeviceDTO;
import io.renren.modules.plc.dto.PlcReadRequestDTO;
import io.renren.modules.plc.dto.PlcWriteRequestDTO;
import io.renren.modules.plc.service.PlcCommunicationService;
import io.renren.modules.plc.service.PlcDeviceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * PLC设备管理及通讯控制器
 */
@RestController
@RequestMapping("/plc")
@Tag(name = "PLC设备管理")
@AllArgsConstructor
public class PlcDeviceController {

    private final PlcDeviceService plcDeviceService;
    private final PlcCommunicationService plcCommunicationService;

    // ==================== 设备管理 ====================

    @GetMapping("page")
    @Operation(summary = "PLC设备分页列表")
    @Parameters({
            @Parameter(name = Constant.PAGE, description = "当前页码，从1开始", in = ParameterIn.QUERY, required = true, ref = "int"),
            @Parameter(name = Constant.LIMIT, description = "每页显示记录数", in = ParameterIn.QUERY, required = true, ref = "int"),
            @Parameter(name = "deviceName", description = "设备名称", in = ParameterIn.QUERY, ref = "String"),
            @Parameter(name = "protocol", description = "协议类型(modbus-tcp/s7)", in = ParameterIn.QUERY, ref = "String")
    })
    @RequiresPermissions("plc:device:page")
    public Result<PageData<PlcDeviceDTO>> page(@Parameter(hidden = true) @RequestParam Map<String, Object> params) {
        PageData<PlcDeviceDTO> page = plcDeviceService.page(params);
        return new Result<PageData<PlcDeviceDTO>>().ok(page);
    }

    @GetMapping("{id}")
    @Operation(summary = "PLC设备详情")
    @RequiresPermissions("plc:device:info")
    public Result<PlcDeviceDTO> info(@PathVariable("id") Long id) {
        PlcDeviceDTO device = plcDeviceService.get(id);
        return new Result<PlcDeviceDTO>().ok(device);
    }

    @PostMapping
    @Operation(summary = "新增PLC设备")
    @LogOperation("新增PLC设备")
    @RequiresPermissions("plc:device:save")
    public Result save(@RequestBody PlcDeviceDTO dto) {
        ValidatorUtils.validateEntity(dto, AddGroup.class, DefaultGroup.class);
        plcDeviceService.save(dto);
        return new Result();
    }

    @PutMapping
    @Operation(summary = "修改PLC设备")
    @LogOperation("修改PLC设备")
    @RequiresPermissions("plc:device:update")
    public Result update(@RequestBody PlcDeviceDTO dto) {
        ValidatorUtils.validateEntity(dto, UpdateGroup.class, DefaultGroup.class);
        plcDeviceService.update(dto);
        return new Result();
    }

    @DeleteMapping
    @Operation(summary = "删除PLC设备")
    @LogOperation("删除PLC设备")
    @RequiresPermissions("plc:device:delete")
    public Result delete(@RequestBody Long[] ids) {
        plcDeviceService.deleteBatch(ids);
        return new Result();
    }

    // ==================== PLC通讯操作 ====================

    @PostMapping("read")
    @Operation(summary = "从PLC读取数据",
            description = "Modbus地址示例：4x000001:INT；S7地址示例：%DB1.DBW0:INT")
    @LogOperation("PLC读取")
    @RequiresPermissions("plc:device:read")
    public Result<Map<String, Object>> read(@RequestBody PlcReadRequestDTO request) {
        ValidatorUtils.validateEntity(request);
        Map<String, Object> data = plcCommunicationService.read(request);
        return new Result<Map<String, Object>>().ok(data);
    }

    @PostMapping("write")
    @Operation(summary = "向PLC写入数据",
            description = "Modbus地址示例：4x000001:INT，值：42；S7地址示例：%DB1.DBW0:INT，值：42")
    @LogOperation("PLC写入")
    @RequiresPermissions("plc:device:write")
    public Result<Map<String, String>> write(@RequestBody PlcWriteRequestDTO request) {
        ValidatorUtils.validateEntity(request);
        Map<String, String> data = plcCommunicationService.write(request);
        return new Result<Map<String, String>>().ok(data);
    }

    @GetMapping("test/{id}")
    @Operation(summary = "测试PLC设备连接")
    @LogOperation("PLC连接测试")
    @RequiresPermissions("plc:device:info")
    public Result<Boolean> testConnection(@PathVariable("id") Long id) {
        boolean connected = plcCommunicationService.testConnection(id);
        return new Result<Boolean>().ok(connected);
    }

    @DeleteMapping("connection/{id}")
    @Operation(summary = "断开并清理PLC设备连接缓存")
    @LogOperation("断开PLC连接")
    @RequiresPermissions("plc:device:update")
    public Result closeConnection(@PathVariable("id") Long id) {
        plcCommunicationService.closeConnection(id);
        return new Result();
    }
}
