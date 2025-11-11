package com.superchat.model;

import com.superchat.interfaces.IChatAgentA;
import com.superchat.interfaces.IProfileExtractionAgent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Contenedor por sesión
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class AiAgentsPerSession {
    private IProfileExtractionAgent profileExtractionAgent;
    private IChatAgentA chatAgentA;
}