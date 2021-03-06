package iisoft.acdp.backend

import grails.gorm.transactions.Transactional
import grails.rest.*
import grails.converters.*

class PublicationController extends RestfulController<Publication> {

    def publicationService

    static responseFormats = ['json']

    PublicationController() {
        super(Publication)
    }

    // get    "/publications"
    def allPublications() {
        respond  publicationService.allPublications()
    }

    //get    "/publication/$idCat"
    def publications(){
        def somePublications = publicationService.getPublicationsOfCategory(params.idCat as long)
        if (somePublications.size() == 0) {
            render status: 404
        }
        else{
            respond somePublications
        }
    }

    //post    "/publication"
    def savePublication(Publication aPublication){
        publicationService.save(aPublication)
    }

    //put   "/publication/subscriber/$userName"
    @Transactional
    def updatePublication(Publication aPublication) {
        def userService = new UserService()
        def anUserName  = params.userName as String
        if(userService.getUserByUserName(anUserName) == null){
            render status: 404
        } else {
            publicationService.subscribe(aPublication, anUserName)
            publicationService.save(aPublication)
            render status: 200
        }
    }


}
