package site.yesaido.ruleengine_server.collector.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SupportedTopic {

    CHIRPSTACK("application/"),
    MUSHROOM("mushroom/");

    private final String prefix;

}
