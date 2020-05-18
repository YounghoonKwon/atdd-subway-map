package wooteco.subway.admin.acceptance;

import java.util.Iterator;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.jdbc.Sql;

import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import wooteco.subway.admin.acceptance.handler.LineHandler;
import wooteco.subway.admin.acceptance.handler.StationHandler;
import wooteco.subway.admin.domain.Station;
import wooteco.subway.admin.dto.LineResponse;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql("/truncate.sql")
public class LineStationAcceptanceTest {
	@LocalServerPort
	int port;

	@Autowired
	private LineHandler lineHandler;

	@Autowired
	private StationHandler stationHandler;

	public static RequestSpecification given() {
		return RestAssured.given().log().all();
	}

	@BeforeEach
	void setUp() {
		RestAssured.port = port;
	}

	/**
	 *     Given 지하철역이 여러 개 추가되어있다.
	 *     And 지하철 노선이 추가되어있다.
	 *
	 *     When 지하철 노선에 지하철역을 등록하는 요청을 한다.
	 *     Then 지하철역이 노선에 추가 되었다.
	 *
	 *     When 지하철 노선의 지하철역 목록 조회 요청을 한다.
	 *     Then 지하철역 목록을 응답 받는다.
	 *     And 새로 추가한 지하철역을 목록에서 찾는다.
	 *
	 *     When 지하철 노선에 포함된 특정 지하철역을 제외하는 요청을 한다.
	 *     Then 지하철역이 노선에서 제거 되었다.
	 *
	 *     When 지하철 노선의 지하철역 목록 조회 요청을 한다.
	 *     Then 지하철역 목록을 응답 받는다.
	 *     And 제외한 지하철역이 목록에 존재하지 않는다.
	 */
	@DisplayName("지하철 노선에서 지하철역 추가 / 제외")
	@Test
	void manageLineStation() {
		//given
		stationHandler.createStation("잠실역");
		stationHandler.createStation("종합운동장역");
		stationHandler.createStation("선릉역");
		stationHandler.createStation("강남역");
		//and
		lineHandler.createLine("신분당선", "blue");
		lineHandler.createLine("1호선", "pink");
		lineHandler.createLine("2호선", "yellow");
		lineHandler.createLine("3호선", "green");

		//when
		lineHandler.addLineStation(1L, 0L, 1L);
		lineHandler.addLineStation(1L, 1L, 2L);
		//then
		List<Station> stations = lineHandler.getLine(1L).getStations();

		Assertions.assertThat(stations.size())
			.isEqualTo(2);

		//when
		LineResponse lineResponse = lineHandler.getLine(1L);
		//then
		stations = lineResponse.getStations();

		//when
		lineHandler.deleteLineStation(1L, 2L);
		//then
		Assertions.assertThat(lineHandler.getLine(1L).getStations().size())
			.isEqualTo(1);

		//when
		lineResponse = lineHandler.getLine(1L);
		//then
		stations = lineResponse.getStations();
		//and
		Iterator<Station> iterator = stations.iterator();
		while (iterator.hasNext()) {
			Assertions.assertThat(iterator.next().getName()).isNotEqualTo("종합운동장역");
		}
	}
}