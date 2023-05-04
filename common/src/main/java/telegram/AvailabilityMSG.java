package telegram;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
public class AvailabilityMSG implements Serializable {
    private String agentId;
    private List<Availability> availabilities;
}
