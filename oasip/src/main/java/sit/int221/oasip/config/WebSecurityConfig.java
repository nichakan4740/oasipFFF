package sit.int221.oasip.config;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import sit.int221.oasip.CustomException.CustomAccessDeniedHandler;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@EnableAutoConfiguration
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Autowired
    private UserDetailsService jwtUserDetailsService;

    @Autowired
    private JwtRequestFilter jwtRequestFilter;

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(jwtUserDetailsService).passwordEncoder(argon2PasswordEncoder());
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Bean
    public Argon2PasswordEncoder argon2PasswordEncoder(){
        return new Argon2PasswordEncoder(16,29,1,16,2);
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler(){
        return new CustomAccessDeniedHandler();
    }

    @Override
    protected void configure(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.csrf().disable().cors().and()
                .exceptionHandling().authenticationEntryPoint(new JwtAuthenticationEntryPoint())
                .accessDeniedHandler(accessDeniedHandler())
                .and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
                .authorizeRequests()
                //login ??????????????????????????????????????????????????????????????????????????? token
                .antMatchers("/api/login","/api/users/signup").permitAll()
                .antMatchers("/api/files/**").permitAll()

                //admin ???????????????????????? user ?????????????????????????????? ???????????????????????????????????? match ?????????
                .antMatchers("/api/users/**","/api/match/**").hasRole("admin")

                //admin ?????????????????? delete user ??????????????????????????????????????????????????????
                .antMatchers(HttpMethod.DELETE, "/api/users/{id}").hasRole("admin")

                //admin student lecturer ???????????????????????? event ????????? eventdetailwithid ?????????
                .antMatchers(HttpMethod.GET,"/api/categories").permitAll()
                .antMatchers(HttpMethod.GET, "/api/events","/api/events/{id}").hasAnyRole("admin","student","lecturer")

                //admin student guest ???????????????????????? add event ?????????
                .antMatchers(HttpMethod.POST, "/api/events").permitAll()
//                .antMatchers(HttpMethod.POST, "/api/events").hasAnyRole("admin","student","guest")

                //admin student ?????????????????? update event ?????????
                .antMatchers(HttpMethod.PUT, "/api/events/{id}").hasAnyRole("admin","student")

                //admin student ?????????????????? delete event ?????????
                .antMatchers(HttpMethod.DELETE, "/api/events/{id}").hasAnyRole("admin","student")

                .anyRequest().authenticated();
        httpSecurity.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
    }
}