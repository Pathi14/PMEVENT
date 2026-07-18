Feature: Gestion des utilisateurs

Background:
    * url baseUrl

Scenario: Récupérer tous les utilisateurs
    Given path '/user/all'
    When method GET
    Then status 200
    And match response == '#[]'

Scenario: CRUD utilisateur

    # Récupérer mon utilisateur connecté
    Given path '/user/me'
    When method GET
    Then status 200
    And match response.email == 'test@test.com'
    * def userId = response.id

    # Modifier l'utilisateur
    Given path '/user', userId
    And multipart field firstname = 'Utilisateur Modifié'
    And multipart field name = 'Test'
    When method PUT
    Then status 200
    And match response.firstname == 'Utilisateur Modifié'

    # Supprimer l'utilisateur
    Given path '/user', userId
    When method DELETE
    Then status 204