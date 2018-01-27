package com.talytica.integration.objects;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.gson.annotations.SerializedName;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class SendGridEmailEvent {
    public String ip;
    public Long sg_user_id;
    public String sg_event_id;
    public String sg_message_id;
    public String useragent;
    public String event;
    public String email;
    public Long timestamp;
    public String asm_group_id;
    public String url;
    
    @SerializedName("smtp-id")
    public String smtpId;
}
