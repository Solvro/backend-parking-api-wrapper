package pl.wrapper.parking.facade.domain.historic;

import io.hypersistence.utils.hibernate.type.array.IntArrayType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.NamedNativeQuery;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;

@Entity
@Getter
@Table(name = "historic_data")
@AllArgsConstructor
@NoArgsConstructor
@NamedQueries({
    @NamedQuery(
            name = "HistoricData.periodQuery",
            query = "SELECT data FROM HistoricDataEntry data WHERE data.date >= :from AND data.date <= :to"),
    @NamedQuery(
            name = "HistoricData.fromQuery",
            query = "SELECT data FROM HistoricDataEntry data WHERE data.date >= :from")
})
@NamedNativeQuery(
        name = "HistoricData.atQuery",
        query = "SELECT data_table FROM historic.historic_data WHERE date = :at")
class HistoricDataEntry {

    @Id
    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Type(value = IntArrayType.class, parameters = @Parameter(name = IntArrayType.SQL_ARRAY_TYPE, value = "smallint"))
    @Column(name = "data_table", columnDefinition = "smallint[][]", nullable = false)
    private short[][] parkingInfo;

    HistoricDataEntry(int parkingCount, int timeframeCount, LocalDate date) {
        this.date = date;
        this.parkingInfo = new short[parkingCount][timeframeCount];
        for (int i = 0; i < parkingCount; i++) {
            for (int j = 0; j < timeframeCount; j++) {
                this.parkingInfo[i][j] = -1;
            }
        }
    }

    public void addValue(int parkingId, int forTimeframe, int freeSpots) {
        this.parkingInfo[parkingId - 1][forTimeframe] = (short) freeSpots;
    }
}
