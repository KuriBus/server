/*package com.chatting.capstone.global;

import com.chatting.capstone.global.moderation.ClovaService;
import com.chatting.capstone.global.moderation.PerplexityService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class test {

    private final ClovaService clovaService;
    private final PerplexityService perplexityService;

    public test(ClovaService clovaService, PerplexityService perplexityService) {
        this.clovaService = clovaService;
        this.perplexityService = perplexityService;
    }

    @GetMapping("/clova/appraise")
    public String appraiseSentence(@RequestParam String sentence) {
        return clovaService.appraiseSentence(sentence);
    }

    @GetMapping("/perplexity/transform")
    public String transformSentence(@RequestParam String sentence) {
        return perplexityService.transformToPositive(sentence);
    }
}*/