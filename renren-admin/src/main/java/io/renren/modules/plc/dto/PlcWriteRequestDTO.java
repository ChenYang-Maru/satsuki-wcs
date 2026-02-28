package io.renren.modules.plc.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * PLC写入请求DTO
 */
@Data
@Schema(title = "PLC写入请求")
public class PlcWriteRequestDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(title = "PLC设备ID")
    @NotNull(message = "设备ID不能为空")
    private Long deviceId;

    @Schema(title = "要写入的标签列表")
    @NotNull(message = "标签列表不能为空")
    private List<PlcWriteTagDTO> tags;

    /**
     * PLC写入标签定义
     */
    @Data
    @Schema(title = "PLC写入标签")
    public static class PlcWriteTagDTO implements Serializable {
        private static final long serialVersionUID = 1L;

        @Schema(title = "标签名称（自定义）")
        @NotBlank(message = "标签名称不能为空")
        private String tagName;

        @Schema(title = "标签地址（如 Modbus: 4x000001:INT，S7: %DB1.DBW0:INT）")
        @NotBlank(message = "标签地址不能为空")
        private String tagAddress;

        @Schema(title = "写入值（字符串形式，根据类型自动转换）")
        @NotBlank(message = "写入值不能为空")
        private String value;

        @Schema(title = "数据类型：BOOL、INT、DINT、REAL、STRING（默认INT）")
        private String dataType;
    }
}
