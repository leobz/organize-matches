package matches.organizer.service;

import matches.organizer.domain.Match;
import matches.organizer.domain.MatchBuilder;
import matches.organizer.domain.Player;
import matches.organizer.domain.User;
import matches.organizer.dto.POSTMatchDTO;
import matches.organizer.dto.CounterDTO;
import matches.organizer.exception.AddPlayerException;
import matches.organizer.exception.MatchNotFoundException;
import matches.organizer.storage.MatchRepository;
import matches.organizer.storage.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class MatchService {

    private final MatchRepository matchRepository;
    private final UserRepository userRepository;

    @Autowired
    public MatchService(MatchRepository matchRepository, UserRepository userRepository) {
        this.matchRepository = matchRepository;
        this.userRepository = userRepository;
    }

    public List<Match> getMatches() {
        return matchRepository.getAll();
    }

    public Match getMatch(UUID id) {
        return matchRepository.get(id);
    }

    public void updateMatch(Match match) {
        matchRepository.update(match);
    }

    public Match createMatch(POSTMatchDTO newMatch){

        Match match = new MatchBuilder()
                .setName(newMatch.getName())
                .setUserId(newMatch.getUserId())
                .setDate(newMatch.getDate())
                .setHour(newMatch.getHour())
                .setLocation(newMatch.getLocation())
                .build();

        matchRepository.add(match);

        return match;
  }


    public List<Player> registerNewPlayer(UUID id, User user, String phone, String email) {
        var match = matchRepository.get(id);

        if(match != null) {
            addPlayerToMatch(match, user, phone, email);
            matchRepository.update(match);

            // TODO capaz estaría bueno loggear algo de esto

            /*
            System.out.println("Se agrega un player al match" + id
            + "\ncon el alias: " + user.getAlias()
            + "\ncon el telefono: " + phone
            + "\ncon el email " + email);
            */
            return match.getPlayers();
        } else {
            throw new MatchNotFoundException("Match: Match not found.");
        }
    }

    /**
     * Retorna un contador con la cantidad de partidos creados y jugadores anotados a partir de una fecha/hora.
     *
     * @param from DateTime a partir del cual se buscaran los partidos y jugadores
     * @see CounterDTO
     */
    public CounterDTO getMatchAndPlayerCounterFrom(LocalDateTime from) {
        List<Match> matches = matchRepository.getAllCreatedFrom(from);
        List<Player> players = matchRepository.getAllPlayersConfirmedFrom(from);

        return new CounterDTO(matches.size(), players.size());
    }

    public void addPlayerToMatch(Match match, User user, String phone, String email) {
        if(phone == null)
            throw new AddPlayerException("Match: Cannot add player. Phone cannot be null.");
        if(email == null)
            throw new AddPlayerException("Match: Cannot add player. Email cannot be null.");
        match.addPlayer(user.getId());
        updateUser(user, phone, email);
    }

    private void updateUser(User user, String phone, String email) {
        user.setPhone(phone);
        user.setEmail(email);
        if (userRepository.get(user.getId()) != null) {
            userRepository.update(user);
        } else {
            userRepository.add(user);
        }
    }
    
}
