db.getCollection('record').aggregate([
{$match: {combinedKey: {$size:1}}},
{$group: {
    _id: "$country",
    confirmed: {$sum: "$confirmed"},
    deaths: {$sum: "$deaths"}}
}
])


db.getCollection('record').find({$and:[{combinedKey: {$size:1}}, {lastUpdated: {$gte: ISODate("2020-04-07T23:00:00.000Z")}}]}).sort({confirmed: -1}) 

// same query using aggregation
db.getCollection('record').aggregate([
{$match: {$and: [{combinedKey: {$size:1}},{lastUpdated: {$gte: ISODate("2020-04-07T23:00:00.000Z")}}]}},
{$sort: {confiremd: -1}}
])

db.getCollection('record').aggregate([
{$match: {"lastUpdated" : ISODate("2020-04-07T23:00:00.000Z")}},
{$group: {
    _id:null,
    confirmed: {$sum: "$confirmed"},
    deaths: {$sum: "$deaths"}
    }}
])

db.getCollection('record').aggregate([
{$match:{$and:[{"lastUpdated" : ISODate("2020-04-09T23:00:00.000Z")},{country:"US"}, {newCases: {$lt:0}}]}},
{$sort: {lastUpdated: -1}}
])

db.getCollection('record').aggregate([
{$match: {$and: [{country:"US"}, {state:"Diamond Princess"}]}},
{$group:{
    _id:"$state",
    confirmed: {$sum:"$confirmed"},
    deaths: {$sum:"$deaths"},
    newCases: {$sum: "$newCases"},
    newDeaths: {$sum: "$newDeaths"}
    }},
 {$sort:{_id: -1}}
])

#check two fields are not matching
db.getCollection('record').find({$where: "this.state != this.country"})

db.getCollection('record').find(
{$and:[{combinedKey: {$size:0}},{$where: "this.state != this.country"}]}
)

// manual update query
// you have to open Robo 3T and do not use the MongoDB Atlas replicaset 
// but use the covid-19 database directily in the MongoDB Atlass in the Robo 3T 
// double click on record collection in the MongoDB Atlas 
// otherwise it will cause WriteCommandError: not master mongodb
db.getCollection('record').find({country:"India"}).sort({lastUpdated:-1})
db.getCollection('record').find({country:"Spain"}).sort({lastUpdated:-1})
db.getCollection('record').updateOne({"_id" : ObjectId("607919d40d22d0673386303b")},{
    $set:{
        "newCases" : NumberLong(216850),
        "newDeaths" : NumberLong(1183),
        "confirmed" : NumberLong(180809),
        "deaths" : NumberLong(28022)
        }
})
