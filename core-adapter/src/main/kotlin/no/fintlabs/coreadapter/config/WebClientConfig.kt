package no.fintlabs.coreadapter.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.oauth2.client.*
import org.springframework.security.oauth2.client.endpoint.WebClientReactivePasswordTokenResponseClient
import org.springframework.security.oauth2.client.registration.ClientRegistration
import org.springframework.security.oauth2.client.registration.InMemoryReactiveClientRegistrationRepository
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction
import org.springframework.security.oauth2.core.AuthorizationGrantType
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

@Configuration
class WebClientConfig(
    private val adapterProps: AdapterProperties,
    private val providerProps: ProviderProperties,
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
    fun webClient(authorizedClientManager: ReactiveOAuth2AuthorizedClientManager): WebClient =
        WebClient
            .builder()
            .filter(createExchangeFilterFunction(authorizedClientManager))
            .baseUrl(providerProps.baseUrl)
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
    fun authorizedClientManager(
        clientRegistrationRepository: ReactiveClientRegistrationRepository,
        authorizedClientService: ReactiveOAuth2AuthorizedClientService,
    ): ReactiveOAuth2AuthorizedClientManager =
        AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager(
            clientRegistrationRepository,
            authorizedClientService,
        ).apply {
            setAuthorizedClientProvider(createAuthorizedClientProvider())
            setContextAttributesMapper { authorized ->
                mutableMapOf<String, Any>()
                    .apply {
                        putAll(authorized.attributes)
                        put(OAuth2AuthorizationContext.USERNAME_ATTRIBUTE_NAME, adapterProps.username)
                        put(OAuth2AuthorizationContext.PASSWORD_ATTRIBUTE_NAME, adapterProps.password)
                    }.let { Mono.just(it) }
            }
        }

    @Bean
    fun passwordClientRegistration(): ClientRegistration =
        ClientRegistration
            .withRegistrationId(REGISTRATION_ID)
            .tokenUri("https://idp.felleskomponent.no/nidp/oauth/nam/token")
            .authorizationGrantType(AuthorizationGrantType.PASSWORD)
            .clientId(adapterProps.clientId)
            .clientSecret(adapterProps.clientSecret)
            .scope(adapterProps.scope)
            .build()

    @Bean
    fun clientRegistrationRepository(passwordClientRegistration: ClientRegistration): ReactiveClientRegistrationRepository =
        InMemoryReactiveClientRegistrationRepository(passwordClientRegistration)

    fun createAuthorizedClientProvider(): ReactiveOAuth2AuthorizedClientProvider =
        ReactiveOAuth2AuthorizedClientProviderBuilder
            .builder()
            .password { it.accessTokenResponseClient(WebClientReactivePasswordTokenResponseClient()) }
            .refreshToken()
            .build()
}
