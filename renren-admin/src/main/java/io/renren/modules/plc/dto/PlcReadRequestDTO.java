package io.renren.modules.plc.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * PLC读取请求DTO
 */
@Data
@Schema(title = "PLC读取请求")
public class PlcReadRequestDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(title = "PLC设备ID")
    @NotNull(message = "设备ID不能为空")
    private Long deviceId;

    @Schema(title = "要读取的标签列表")
    @NotNull(message = "标签列表不能为空")
    private List<PlcTagDTO> tags;

    /**
     * PLC标签定义
     */
    @Data
    @Schema(title = "PLC标签")
    public static class PlcTagDTO implements Serializable {
        private static final long serialVersionUID = 1L;

        @Schema(title = "标签名称（自定义，用于区分返回值）")
        @NotBlank(message = "标签名称不能为空")
        private String tagName;

        @Schema(title = "标签地址（如 Modbus: 4x000001:INT，S7: %DB1.DBW0:INT）")
        @NotBlank(message = "标签地址不能为空")
        private String tagAddress;
    }
}
