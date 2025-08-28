package de.bushnaq.abdalla.engine.audio.radio;

/**
 * @param radioMessage populated only in the last wave for this message
 * @param radioMessage populated only in the last wave for this message
 */
public record RadioWave(byte[] wavFileBytes, RadioMessage radioMessage, int index) {
    boolean isLastWave() {
        return index == radioMessage.getMessages().size() - 1;
    }

}
