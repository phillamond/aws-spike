Feature: Email notification service
  As an end user
  In order to listen to new music by my favourite artists
  I need to be notified when they release a new track

  Scenario: User subscription to email notification service
    Given a running artist email notification ReST service
    When I make a PUT request to "/subscribe" with input body
    """
    {
      "userEmailAddress": "joebloggs@sonyentertainment.com",
      "artistId": "test:b12a908e7e224f6892fee6a8210b7d02"
    }
    """
    Then I should be returned a response status of "201 Created"

  Scenario: Update of artist profile with a new track
    Given a running artist email notification ReST service
    When Pink Floyd release a new track
    And a notification is POSTed to "/events/test:b12a908e7e224f6892fee6a8210b7d02" with input body
    """
    {
        "type": "Catalogue",
        "occurred": "2013-11-18T21:33:55.373Z",
        "message": {
            "method": "UpdateArtist",
            "params": {
                "artistId": "test:b12a908e7e224f6892fee6a8210b7d02",
                "tid": "acdc73c9c8da4351ba54ebc28d843484",
                "artist": {
                    "artistId": "test:b12a908e7e224f6892fee6a8210b7d02",
                    "imageUrl": "http://artcdn.ribob01.net/images/3ad4",
                    "artistName": "Pink Floyd",
                    "localArtistNames": [
                        {
                            "language": "EN",
                            "script": "latin",
                            "country": "GB",
                            "value": "Pink Floyd"
                        }
                    ]
                }
            }
        }
    }
    """
    Then the list of email subscribers are sent and email
    And the email body will contain "There is new content by artist Pink Floyd"