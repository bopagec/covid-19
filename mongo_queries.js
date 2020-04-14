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