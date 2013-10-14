package com.whatamidoing.utils

import com.typesafe.config._ 

object ApplicationProps {

    /** ConfigFactory.load() defaults to the following in order: 
      * system properties 
      * application.conf 
      * application.json 
      * application.properties 
      * reference.conf 
      * 
      * So a system property set in the application will override file properties 
      * if it is set before ConfigFactory.load is called. 
      * eg System.setProperty("environment", "production") 
      */ 
    val envConfig = ConfigFactory.load("application")
  //val envConfig = ConfigFactory.load()
 
    val environment =   envConfig getString "thisIsWhatIAmDoing.environment" 
  	
    /** ConfigFactory.load(String) can load other files. 
      * File extension must be conf, json, or properties. 
      * The extension can be omitted in the load argument. 
      */ 
    val config = ConfigFactory.load(environment) // eg "test" or "test.conf" etc 
 
    /** Libraries and frameworks should contain a reference.conf 
      * which can then be validated using: 
      * config.checkValid(ConfigFactory.defaultReference(), "configurableApp") 
      */ 
 
    val neo4jServer = config getString "neo4jServer" 
    
    //Constants from message file
    val noTokenProvided = play.api.i18n.Messages("no.token.provided")
    val noEmailProvided = play.api.i18n.Messages("no.email.provided")
 
}