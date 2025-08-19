package de.bushnaq.abdalla.engine.audio;

import de.bushnaq.abdalla.engine.ai.PromptTags;
import lombok.Getter;

@Getter
public class RadioRequest {
    private final CommunicationPartner from;
    private final String               messageId;
    private final RadioMessageId       radioMessageId;
    private final boolean              silent;
    private final PromptTags           tags;
    private final CommunicationPartner to;

    public RadioRequest(boolean silent, CommunicationPartner from, CommunicationPartner to, RadioMessageId radioMessageId, String messageId, PromptTags tags) {
        this.silent         = silent;
        this.from           = from;
        this.to             = to;
        this.radioMessageId = radioMessageId;
        this.messageId      = messageId;
        this.tags           = tags;
    }
}
