package telegram;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
public class Availability implements Serializable {
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
}
