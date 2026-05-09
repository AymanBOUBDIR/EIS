package ma.enset.eisbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AgentQueryResponse {
    private String query;
    private String response;
    private Long timestamp;
    private String error;
}
