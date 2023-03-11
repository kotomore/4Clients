package ru.set404.clients.dto;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@Setter
public class AvailabilitiesDTO {
    @JsonFormat(pattern="yyyy-MM-dd HH:mm")
    private LocalDateTime startTime;
    @JsonFormat(pattern="yyyy-MM-dd HH:mm")
    private LocalDateTime endTime;

    public AvailabilitiesDTO() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AvailabilitiesDTO that = (AvailabilitiesDTO) o;
        return Objects.equals(startTime, that.startTime) && Objects.equals(endTime, that.endTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(startTime, endTime);
    }
}
