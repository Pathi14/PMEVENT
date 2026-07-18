Feature: Gestion des évènements

  Background:
    * url baseUrl

  Scenario: Récupérer un évènement public
    Given path '/events/public'
    When method GET
    Then status 200
    And match response == '#[]'


  Scenario: Récupérer les évènements accessibles aux invités
    Given path '/events/guest'
    When method GET
    Then status 200
    And match response == '#[]'


  Scenario: Récupérer les évènements administrateurs
    Given path '/events/administrator'
    When method GET
    Then status 200
    And match response == '#[]'


  Scenario: CRUD un nouvel évènement
    Given path '/events/new-event'
    And multipart field name = 'Concert Test'
    And multipart field description = 'Un évènement créé avec Karate'
    And multipart field location = 'Paris'
    And multipart field date = '2026-01-01'
    When method POST
    Then status 200
    And match response.name == 'Concert Test'
    * def createdId = response.id

  # Récupérer un évènement par son id
    Given path '/events', createdId
    When method GET
    Then status 200
    And match response.id == createdId

  # Modifier un évènement
    Given path '/events/update-event', createdId
    And multipart field name = 'Concert Modifié'
    When method PUT
    Then status 200
    And match response.name == 'Concert Modifié'

  # Supprimer un évènement
    Given path '/events', createdId
    When method DELETE
    Then status 204