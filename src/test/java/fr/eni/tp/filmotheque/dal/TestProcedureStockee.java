package fr.eni.tp.filmotheque.dal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;

import fr.eni.tp.filmotheque.bo.Participant;

@SpringBootTest
@TestMethodOrder(MethodOrderer.MethodName.class)
public class TestProcedureStockee {
	protected final Log logger = LogFactory.getLog(getClass());
	
	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	@SuppressWarnings("unchecked")
	void testProcedure_SimpleJdbcCall() {
		SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
					.withProcedureName("FindActeurs")
					.returningResultSet("acteurs", new BeanPropertyRowMapper<Participant>(Participant.class));

		long idFilm = 4;
		SqlParameterSource in = new MapSqlParameterSource().addValue("idFilm", idFilm);
		
		Map<String, Object> out = jdbcCall.execute(in);

		assertNotNull(out.get("acteurs"));
		List<Participant> acteurs = (List<Participant>) out.get("acteurs");
		assertNotNull(acteurs);
		assertEquals(2, acteurs.size());
		acteurs.forEach(a -> logger.info(a));
	}

}
