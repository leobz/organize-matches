package matches.organizer.service;

import matches.organizer.domain.Match;
import matches.organizer.domain.MatchBuilder;
import matches.organizer.domain.Player;
import matches.organizer.domain.User;
import matches.organizer.dto.CounterDTO;
import matches.organizer.storage.MatchRepository;
import matches.organizer.storage.PlayerRepository;
import matches.organizer.storage.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Service
public class MatchService {

    MatchRepository matchRepository;
    UserRepository userRepository;

    PlayerRepository playerRepository;

    MongoTemplate mongoTemplate;

    Logger logger = LoggerFactory.getLogger(MatchService.class);

    @Autowired
    public MatchService(MatchRepository matchRepository, UserRepository userRepository,PlayerRepository playerRepository,MongoTemplate mongoTemplate ) {
        this.matchRepository = matchRepository;
        this.userRepository = userRepository;
        this.playerRepository = playerRepository;
        this.mongoTemplate = mongoTemplate;
    }

    public List<Match> getMatches() {
        return matchRepository.findAll();
    }

    public Match getMatch(String id) {
        return matchRepository.findById(id).orElse(null);
    }

    public void updateMatch(Match match) {
        matchRepository.save(match);
    }

    public Match createMatch(Match newMatch) {
        logger.info("BEGGINING createMatch() function");

        if (userRepository.findById(newMatch.getUserId()).orElse(null).equals(null)) {

            logger.error("USER NOT FOUND: NEED TO CREATE AND USER BEFORE");
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");

        }
        logger.info("BEGGINING Builder");

        Match match = new MatchBuilder()
                .setName(newMatch.getName())
                .setUserId(newMatch.getUserId())
                .setDateAndTime(newMatch.getDateAndTime())
                .setLocation(newMatch.getLocation())
                .build();

        logger.info("TRYING INSEERT A MATCH  WITH ID: {}", match.getId());
        matchRepository.save(match);
        logger.info("NEW MATCH CREATED WITH ID: {}", match.getId());
        return match;
    }

    public Match editMatch(String matchId, Match newMatch) {
        Match oldMatch = matchRepository.findById(matchId).orElse(null);
        if (oldMatch == null) {
            logger.error("MATCH NOT FOUND: CANNOT EDIT MATCH");
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Match not foundL cannot edit match");
        }

        Match editedMatch = new MatchBuilder().fromMatch(oldMatch)
                .setName(newMatch.getName())
                .setDateAndTime(newMatch.getDateAndTime())
                .setLocation(newMatch.getLocation())
                .build();

        matchRepository.save(editedMatch);
        logger.info("UPDATED MATCH WITH ID: " + editedMatch.getId());
        return editedMatch;
    }


    public static Match createRandomMatch() {
        return new MatchBuilder().
                setName("Match").
                setLocation("Location").
                setUserId(UUID.randomUUID().toString()).
                setDateAndTime(LocalDateTime.now(ZoneOffset.UTC).plusDays(1))
                .build();
    }

    public void createAndSaveRandomMatch() {
        Match match = createRandomMatch();
        matchRepository.save(match);
    }

    public void registerNewPlayer(String id, User user) {
        Match match = matchRepository.findById(id).orElse(null);
        logger.error("LEST TRY WITH: {} , {}", user.getId(), match.getId());

        if (match != null) {
            addPlayerToMatch(match, user);
            matchRepository.save(match);

            //Se crea nuevo player con atributo crearable distinto de nulo para que se cree indice de eliminacion automatica.
            Player newPlayer = new Player(user.getId(), user.getAlias());
            newPlayer.setClearable(true);
            logger.info("TRYING TO SAVE A NEW PLAYER");

            playerRepository.save(newPlayer);
            logger.info("PLAYER WITH ID: {} ADDED CORRECTLY TO MATCH: {}", user.getId(), match.getId());

        } else {
            logger.error("MATCH NOT FOUND WITH ID: {}", id);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Match not found.");
        }
    }

    public List<Player> unregisterPlayer(String matchId, String playerId) {
        Match match = matchRepository.findById(matchId).orElse(null);

        if (match != null) {
            match.removePlayer(playerId);
            matchRepository.save(match);
            logger.info("PLAYER WITH ID: " + playerId + " REMOVED CORRECTLY FROM MATCH " + match.getId());
            return match.getPlayers();
        } else {
            logger.error("MATCH NOT FOUND WITH ID: " + matchId.toString());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Match not found.");
        }
    }

    /**
     * Retorna un contador con la cantidad de partidos creados y jugadores anotados a partir de una fecha/hora.
     *
     * @param from DateTime a partir del cual se buscaran los partidos y jugadores
     * @see CounterDTO
     */
    public CounterDTO getMatchAndPlayerCounterFrom(LocalDateTime from) {
        List<Match> matches = matchRepository.findByCreatedAtAfter(from);
        List<Player> players = playerRepository.findAll();
        logger.info("{} MATCHES AND {} PLAYERS CONFIRMED IN THE LAST TWO HOURS ", matches.size(), players.size());
        return new CounterDTO(matches.size(), players.size());
    }

    public void addPlayerToMatch(Match match, User user) {
        if (user.getPhone() == null) {
            logger.error("CANNOT ADD PLAYER IF PHONE NUMBER IS NULL.");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Match: Cannot add player. Phone cannot be null.");
        }
        if (user.getEmail() == null) {
            logger.error("CANNOT ADD PLAYER IF EMAIL IS NULL.");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Match: Cannot add player. Email cannot be null.");
        }
        match.addPlayer(user);
        updateUser(user);
    }

    private void updateUser(User user) {
        if (userRepository.findById(user.getId()).isEmpty()) {
            logger.error("USER DOES NOT EXISTS.");
        }
        userRepository.save(user);
    }

}