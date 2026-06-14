package ma.enset.eisbackend.controller;

import lombok.RequiredArgsConstructor;
import ma.enset.eisbackend.dto.AgentQueryRequest;
import ma.enset.eisbackend.dto.AgentQueryResponse;
import ma.enset.eisbackend.service.AgentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/agent")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class AgentController {

    private final AgentService agentService;

    @PostMapping("/query")
    public ResponseEntity<AgentQueryResponse> processQuery(@RequestBody AgentQueryRequest request) {
        AgentQueryResponse response = agentService.processQuery(request);
        return ResponseEntity.ok(response);
    }
}
