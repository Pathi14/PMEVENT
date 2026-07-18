Feature: Gestion des invités

  Background:
    * url baseUrl

  Scenario: Récupérer les invités d'un évènement
    Given path '/guests/event/1'
    When method GET
    Then status 200
    And match response == '#[]'


  Scenario: CRUD d'un invité

    # Ajouter un invité à l'évènement 45
    Given path '/guests/event/45/new-guest'
    And multipart field name = 'Dupont'
    And multipart field firstname = 'Jean'
    And multipart field email = 'jean.dupont@test.com'
    And multipart field number_places = '2'
    When method POST
    Then status 200
    And match response.email == 'jean.dupont@test.com'
    * def guestId = response.id

    # Récupérer l'invité par son id
    Given path '/guests', guestId
    When method GET
    Then status 200
    And match response.id == guestId

    # Modifier l'invité
    Given path '/guests', guestId
    And multipart field name = 'MARTIN'
    And multipart field firstname = 'Jean'
    When method PUT
    Then status 200
    And match response.name == 'MARTIN'

    # Vérifier le QR code
    Given path '/guests', guestId, 'qrcode'
    When method GET
    Then status 200
    And match response == '#[]'

    # Marquer présent
    Given path '/guests', guestId, 'present'
    When method POST
    Then status 200

    # Supprimer l'invité
    Given path '/guests', guestId
    When method DELETE
    Then status 204