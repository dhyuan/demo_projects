

    mongosh --port 27017 -u admin -p password --authenticationDatabase admin
    
    use cas_db
    
    db.createUser({
        user: "casdb_user",
        pwd: "password",
        roles: [ { role: "readWrite", db: "cas_db" } ]
        })
    
    
    db.cas_user.insert({
        "username": "casuser",
        "password": "$2a$10$H3ilhOY.s/.U0IKapuwvCO8PwAG3LzvtqLNzYQR3.0ZatLDkIoTwq",
        "first_name": "john",
        "last_name": "smith"
    })
    
