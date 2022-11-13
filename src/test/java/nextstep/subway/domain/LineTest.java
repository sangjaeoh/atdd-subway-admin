package nextstep.subway.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.junit.jupiter.api.Assertions.assertAll;

public class LineTest {

    @Test
    @DisplayName("노선은 이름, 색깔, 상행종점, 하행종점을 받아 생성한다")
    void 생성() {
        // given
        String name = "8호선";
        String color = "분홍색";
        Station upStation = new Station("잠실역");
        Station downStation = new Station("장지역");

        // when
        Line line = Line.of(name, color, upStation, downStation);

        // then
        assertThat(line).isNotNull();
    }

    @Test
    @DisplayName("노선의 생성 시 이름, 색깔이 비어있으면 예외 발생")
    void 생성시_필수값_예외() {
        // given
        Station upStation = new Station("잠실역");
        Station downStation = new Station("장지역");

        // expect
        assertAll(
                () -> assertThatIllegalArgumentException().isThrownBy(() -> Line.of("이름", null, upStation, downStation)),
                () -> assertThatIllegalArgumentException().isThrownBy(() -> Line.of(null, "색깔", upStation, downStation))
        );
    }
}