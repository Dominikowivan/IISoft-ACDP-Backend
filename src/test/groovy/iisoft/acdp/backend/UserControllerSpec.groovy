package iisoft.acdp.backend

import grails.converters.JSON
import grails.plugin.springsecurity.SpringSecurityService
import grails.test.hibernate.HibernateSpec
import grails.testing.web.controllers.ControllerUnitTest
import groovy.json.JsonBuilder
import iisoft.acdp.backend.authentication.NormalUser
import iisoft.acdp.backend.authentication.Role

import static groovy.util.GroovyTestCase.*

class UserControllerSpec extends HibernateSpec implements ControllerUnitTest<UserController> {

    UserProfile           pepitaProfile
    UserService           anUserService
    SpringSecurityService anMockSpringSecurityService
    def             aJsonBuilder
    def             users

    def setup() {
        aJsonBuilder = new JsonBuilder()
        pepitaProfile= new UserProfile(userName: "PepitaUser", name:"pepita", surname: "Swallow", mail: "pepita@gmail.com", birthDate: new Date(2018, 06, 22),userID: 1 ).save()

        users = [pepitaProfile]
        anUserService = new UserService()
        anMockSpringSecurityService = Mock(SpringSecurityService)

        controller.userService = anUserService
        controller.springSecurityService= anMockSpringSecurityService
    }

    void "when the controller is requested for all users, it returns all users"() {
        given:
            def someUsers = users.collect {
                def pepita = it
                def jsonBuilder = aJsonBuilder
                jsonBuilder {
                    id        pepita.id
                    userID    pepita.userID
                    name      pepita.name
                    userName  pepita.userName
                    mail      pepita.mail
                    birthDate pepita.birthDate
                    surname   pepita.surname
                }
            } as JSON


        when:
            controller.allUsers()

        then:
         assertEquals(200, response.status)
         assertEquals(someUsers.toString(), response.contentAsString)

    }

    void "when the controller is requested for the users of an userName it returns that user"() {
        given:
            def pepita2 = new UserProfile(name:"Pepita2", surname: "Swallow2", userName: "PepitaUser2",
                    mail: "pepita2@gmail.com", birthDate: new Date(2018, 06, 22),userID: 1)
            pepita2.save()

            def pepitaJson = aJsonBuilder {
                id        pepitaProfile.id
                userID    pepitaProfile.userID
                name      pepitaProfile.name
                userName  pepitaProfile.userName
                mail      pepitaProfile.mail
                birthDate pepitaProfile.birthDate
                surname   pepitaProfile.surname
            } as JSON

            params.userName = "PepitaUser"

        when:
            controller.getUserByUserName()

        then:
            assertEquals(200, response.status)
            assertEquals(pepitaJson.toString(), response.contentAsString)
    }

    void "when the controller is requested for the users of an userName and it has no users, it returns 404"() {
        given:
            params.userName = "none"

        when:
            controller.getUserByUserName()

        then:
            assertEquals(404, response.status)
    }

    void "when requested to save a profile and its the profile of the currently logged in user, it is saved"() {
        given:
            NormalUser currentUser = new NormalUser(username: "pepito",password: "azul").save()
            anMockSpringSecurityService.currentUser >> currentUser

            def aUserJson = aJsonBuilder {
                name      "goku"
                userID     currentUser.id
                userName  "gokuUser"
                surname   "kakaroto"
                mail      "goku@gmail.com"
                birthDate new Date(2018, 06, 22)
            } as JSON

            request.setMethod("POST")
            request.setJSON(aUserJson)

        when:
            controller.saveUser()

        then:
            assertEquals(200, response.status)
            assertNotNull(UserProfile.findByUserName("gokuUser"))
    }

    void "when requested to save a profile and its not the profile of the currently logged in user, it returns 403"() {
        given:
        NormalUser currentUser = new NormalUser(username: "pepito",password: "azul").save()
        anMockSpringSecurityService.currentUser >> currentUser
        def aUserJson = aJsonBuilder {
            name      "goku"
            userID     123123
            userName  "gokuUser"
            surname   "kakaroto"
            mail      "goku@gmail.com"
            birthDate new Date(2018, 06, 22)
        } as JSON

        request.setMethod("POST")
        request.setJSON(aUserJson)

        when:
        controller.saveUser()

        then:
        assertEquals(403, response.status)
    }

    void "when requested to register an user, it is saved"() {
        given:
        new Role(authority:'ROLE_NORMAL_USER').save()
        def aNewUserJson = aJsonBuilder {
            name      "goku"
            password  "pass"
            userName  "gokuUser"
            surname   "kakaroto"
            mail      "goku@gmail.com"
            birthDate new Date(2018, 06, 22)
        } as JSON

        request.setMethod("POST")
        request.setJSON(aNewUserJson)

        when:
        controller.newUser()

        then:
        assertEquals(200, response.status)
        assertNotNull(UserProfile.findByUserName("gokuUser"))
    }

}
