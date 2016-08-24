package auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.ClientRegistrationException;
import org.springframework.security.oauth2.provider.client.BaseClientDetails;
import org.springframework.stereotype.Service;

import java.util.stream.Stream;

@EnableResourceServer
@SpringBootApplication
public class AuthServiceApplication {

    //@Bean
    UserDetailsService userDetailsService(AccountRepository accountRepository) {
        return username -> accountRepository.findByUsername(username)
                .map(account -> {
                    boolean active = account.isActive();
                    return new User(
                            account.getUsername(),
                            account.getPassword(),
                            active, active, active, active,
                            AuthorityUtils.createAuthorityList("ROLE_ADMIN", "ROLE_USER"));
                })
                .orElseThrow(() -> new UsernameNotFoundException(String.format("username %s not found!", username)));
    }

    //@Bean
    ClientDetailsService clientDetailsService(ClientRepository clientRepository) {
        return clientId -> clientRepository.findByClientId(clientId)
                .map(client -> {
                    BaseClientDetails details = new BaseClientDetails(client.getClientId(), null, client.getScopes(),
                            client.getAuthorizedGrantTypes(), client.getAuthorities());
                    details.setClientSecret(client.getSecret());
                    return details;
                })
                .orElseThrow(() -> new ClientRegistrationException(String.format("no client %s registered", clientId)));
    }

    @Service
    public static class JpaClientDetailsService implements ClientDetailsService {

        private final ClientRepository clientRepository  ;

        @Autowired
        public JpaClientDetailsService(ClientRepository clientRepository) {
            this.clientRepository = clientRepository;
        }

        @Override
        public ClientDetails loadClientByClientId(String clientId) throws ClientRegistrationException {
            return clientRepository.findByClientId(clientId)
                    .map(client -> {
                        BaseClientDetails details = new BaseClientDetails(client.getClientId(), null, client.getScopes(),
                                client.getAuthorizedGrantTypes(), client.getAuthorities());
                        details.setClientSecret(client.getSecret());
                        return details;
                    })
                    .orElseThrow(() -> new ClientRegistrationException(String.format("no client %s registered", clientId)));

        }
    }

    @Service
    public static class JpaUserDetailsService implements UserDetailsService {

        private final AccountRepository accountRepository;

        @Autowired
        public JpaUserDetailsService(AccountRepository accountRepository) {
            this.accountRepository = accountRepository;
        }

        @Override
        public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
            return accountRepository.findByUsername(username)
                    .map(account -> {
                        boolean active = account.isActive();
                        return new User(
                                account.getUsername(),
                                account.getPassword(),
                                active, active, active, active,
                                AuthorityUtils.createAuthorityList("ROLE_ADMIN", "ROLE_USER"));
                    })
                    .orElseThrow(() -> new UsernameNotFoundException(String.format("username %s not found!", username)));
        }
    }


    @Bean
    CommandLineRunner data(ClientRepository clientRepository,
                           AccountRepository accountRepository) {
        return args -> {

            Stream.of("jlong,spring", "dsyer,cloud", "pwebb,boot", "mminella,batch", "rwinch,security")
                    .map(s -> s.split(","))
                    .forEach(tuple -> accountRepository.save(new Account(tuple[0], tuple[1], true)));

            Stream.of("acme,acmesecret")
                    .map(x -> x.split(","))
                    .forEach(x -> clientRepository.save(new Client(x[0], x[1])));
        };
    }

    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }
}



