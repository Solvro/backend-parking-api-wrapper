package pl.wrapper.parking.facade.domain.historic;

import io.hypersistence.utils.hibernate.type.array.IntArrayType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;

import java.time.LocalDate;

@Entity
@Getter
@Table(name = "historic_data")
@AllArgsConstructor
@NoArgsConstructor
class HistoricDataEntry {

    @Id
    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Type(
            value = IntArrayType.class,
            parameters = @Parameter(name = IntArrayType.SQL_ARRAY_TYPE, value = "smallint")
    )
    @Column(name = "data_table", columnDefinition = "smallint[][]", nullable = false)
    private short[][] parkingInfo;

    HistoricDataEntry(int parkingCount, int timeframeCount, LocalDate date){
        this.date = date;
        this.parkingInfo = new short[parkingCount][timeframeCount];
        for(int i = 0; i < parkingCount; i++){
            for(int j = 0; j < timeframeCount; j++){
                this.parkingInfo[i][j] = -1;
            }
        }
    }

    public void addValue(int parkingId, int forTimeframe , int freeSpots){
        this.parkingInfo[parkingId][forTimeframe] = (short) freeSpots;
    }
}