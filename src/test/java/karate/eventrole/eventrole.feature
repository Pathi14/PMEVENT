Feature: Gestion des rôles utilisateurs sur les évènements

Background:
    * url baseUrl

Scenario: Gestion complète des rôles d'un évènement

    # Récupérer les rôles existants
    Given path '/users-roles/event/c3aa95af-71d6-4735-b5f0-c020f17549e3'
    When method GET
    Then status 200

    # Voir mon rôle
    Given path '/users-roles/my-role/c3aa95af-71d6-4735-b5f0-c020f17549e3'
    When method GET
    Then status 200

    # Assigner VIEWER
    Given path '/users-roles/assign-role'
    And request
    """
    {
        "eventId":"c3aa95af-71d6-4735-b5f0-c020f17549e3",
        "userId":"c3aa95af-71d6-4735-b5f0-c020f17549e2",
        "role":"VIEWER"
    }
    """
    When method POST
    Then status 200

    # Vérifier ajout VIEWER
    Given path '/users-roles/event/c3aa95af-71d6-4735-b5f0-c020f17549e3'
    When method GET
    Then status 200
    And match response[1].role == 'VIEWER'

    # Supprimer VIEWER
    Given path '/users-roles/remove-role'
    And param eventId = "c3aa95af-71d6-4735-b5f0-c020f17549e3"
    And param userId = "c3aa95af-71d6-4735-b5f0-c020f17549e2"
    When method DELETE
    Then status 200

    # Vérifier suppression
    Given path '/users-roles/event/c3aa95af-71d6-4735-b5f0-c020f17549e3'
    When method GET
    Then status 200