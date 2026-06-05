package no.fintlabs.adapter.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.oauth2.client.*
import org.springframework.security.oauth2.client.registration.ClientRegistration
import org.springframework.security.oauth2.client.registration.InMemoryReactiveClientRegistrationRepository
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction
import org.springframework.security.oauth2.core.AuthorizationGrantType
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class WebClientConfig(
    private val props: DynaAdapterProperties,
) {
    companion object {
        const val REGISTRATION_ID = "fint-adapter"
    }

    /**
     * Every request automatically:
     *  - adds OAuth2 Bearer token from FINT IDP
     *  - refreshes token if expired
     */
    @Bean
    fun dynaWebClient(authorizedClientManager: ReactiveOAuth2AuthorizedClientManager): WebClient =
        WebClient
            .builder()
            .filter(createExchangeFilterFunction(authorizedClientManager))
            .baseUrl(props.baseUrl + ".provider")
            .build()

    private fun createExchangeFilterFunction(authorizedClientManager: ReactiveOAuth2AuthorizedClientManager) =
        ServerOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager)
            .also { it.setDefaultClientRegistrationId(REGISTRATION_ID) }

    /**
     * This manager:
     *  ✔ retrieves tokens via password grant
     *  ✔ caches them
     *  ✔ refreshes tokens automatically when expired
     */
    @Bean
    fun dynaAuthorizedClientManager(
        clientRegistrationRepository: ReactiveClientRegistrationRepository,
        authorizedClientService: ReactiveOAuth2AuthorizedClientService,
    ): ReactiveOAuth2AuthorizedClientManager =
        AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager(
            clientRegistrationRepository,
            authorizedClientService,
        ).apply {
            setAuthorizedClientProvider(
                ReactiveOAuth2AuthorizedClientProviderBuilder.builder()
                    .provider(createAuthorizedClientProvider())
                    .refreshToken()
                    .build()
            )
        }

    @Bean
    fun passwordClientRegistration(): ClientRegistration =
        ClientRegistration.withRegistrationId(REGISTRATION_ID)
            .tokenUri("https://idp.felleskomponent.no/nidp/oauth/nam/token")
            .authorizationGrantType(AuthorizationGrantType("password"))
            .clientId(props.clientId)
            .clientSecret(props.clientSecret)
            .scope(props.scope)
            .build()

    @Bean
    fun clientRegistrationRepository(passwordClientRegistration: ClientRegistration): ReactiveClientRegistrationRepository =
        InMemoryReactiveClientRegistrationRepository(passwordClientRegistration)

    fun createAuthorizedClientProvider(): ReactiveOAuth2AuthorizedClientProvider =
        PasswordReactiveOAuth2AuthorizedClientProvider(
            webClient = WebClient.builder().build(),
            username = props.username,
            password = props.password,
            props = props,
        )

}
