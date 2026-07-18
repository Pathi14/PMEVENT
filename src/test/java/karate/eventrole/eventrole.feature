Feature: Gestion des rôles utilisateurs sur les évènements

Background:
    * url baseUrl

Scenario: Gestion complète des rôles d'un évènement

    # Récupérer les rôles existants
    Given path '/users-roles/event/45'
    When method GET
    Then status 200

    # Voir mon rôle
    Given path '/users-roles/my-role/45'
    When method GET
    Then status 200

    # Assigner VIEWER
    Given path '/users-roles/assign-role'
    And request
    """
    {
        "eventId":45,
        "userId":25,
        "role":"VIEWER"
    }
    """
    When method POST
    Then status 200

    # Vérifier ajout VIEWER
    Given path '/users-roles/event/45'
    When method GET
    Then status 200
    And match response[1].role == 'VIEWER'

    # Supprimer VIEWER
    Given path '/users-roles/remove-role'
    And param eventId = 45
    And param userId = 25
    When method DELETE
    Then status 200

    # Vérifier suppression
    Given path '/users-roles/event/45'
    When method GET
    Then status 200