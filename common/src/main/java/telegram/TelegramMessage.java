package telegram;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TelegramMessage implements Serializable {
    private String agentId;
    private Action action;

    public enum Action {
        REGISTER_BOT,
        AGENT_INFO,
        SERVICE_INFO,
        SCHEDULES,
        SCHEDULE_DELETE,
        APPOINTMENTS,
        APPOINTMENTS_DELETE
    }
}

