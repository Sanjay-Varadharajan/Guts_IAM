package com.guts.Guts_IAM.risk.result;


import com.guts.Guts_IAM.risk.level.RiskLevel;
import lombok.Builder;
import lombok.Data;

import java.util.List;


@Data
@Builder
public class RiskAnalysisResult {
    private Integer riskScore;

    private RiskLevel riskLevel;

    private List<String> reason;

}
