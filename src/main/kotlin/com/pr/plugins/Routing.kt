package com.pr.plugins

import com.pr.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.server.routing.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

fun Application.configureRouting() {

    routing {
        post("/register") {
            val data = call.receive<String>()
            val rest = Json.decodeFromString(Restaurant.serializer(), data)
            call.respondText("Registered", status = HttpStatusCode.Created)
            restaurantList[rest.restaurant_id] = rest
            println("Restaurant ${rest.restaurant_id} registered")

        }

        get("/menu"){
            var restDetails = ArrayList<RestaurantSmall>()
            for (r in restaurantList.values){
                restDetails.add(RestaurantSmall(r.restaurant_id, r.name, r.menuItems, r.menu, r.rating))
            }
            var restList = RestaurantList(restaurantList.size, restDetails)
            call.respond(Json.encodeToString(RestaurantList.serializer(), restList))
            println("Client asked for Menu")
        }
        post("/order"){
            val data = call.receive<String>()
            println("Client placed order $data")
            val orderList = Json.decodeFromString(ClientOrderList.serializer(), data)
            var orders = ArrayList<ClientOrderResponse>()
            for (ord in orderList.orders){
                var ro = RestaurantOrder(ord.items, ord.priority, ord.max_wait, ord.created_time)
                //var restResp = sendToRestaurant(ord.restaurant_id, ro)
                var resp :HttpResponse
                println(restaurantList[ord.restaurant_id]?.address)
                resp = client.post(restaurantList[ord.restaurant_id]?.address + "/v2/order"){
                    setBody(Json.encodeToString(RestaurantOrder.serializer(), ro))
                }
                var restResp = Json.decodeFromString(RestaurantOrderResponse.serializer(), resp.body())

                var tempOrd = restaurantList[restResp.restaurant_id]?.address?.let { it1 ->
                    ClientOrderResponse(restResp.restaurant_id,
                        it1, restResp.order_id, restResp.estimated_waiting_time, restResp.created_time, restResp.registered_time)
                }
                if (tempOrd != null) {
                    orders.add(tempOrd)
                }
            }
            var clResp = ClientResponseList((0..9999).random(), orders)
            call.respond(Json.encodeToString(ClientResponseList.serializer(), clResp))

        }
        post("/rating"){
            val data = call.receive<String>()
            call.respond(HttpStatusCode.NoContent)
            val rateRequestList = Json.decodeFromString(RatingRequestList.serializer(), data)
            for (request in rateRequestList.orders){
                println("Sending rating $request to restaurant ${request.restaurant_id}")
                var rating = ClientRating(request.order_id, request.rating, request.estimated_waiting_time, request.waiting_time)
                var resp : HttpResponse = client.post((restaurantList[request.restaurant_id]?.address ?: "") + "/v2/rating"){
                    setBody(rating)
                }
                var currRating = Json.decodeFromString(ClientRatingResponse.serializer(), resp.body())
                println("Received updated Rating from restaurant $currRating")
                restaurantList[currRating.restaurant_id]?.rating = currRating.restaurant_avg_rating
            }
        }
    }
}

//suspend fun sendToRestaurant(id:Int, ord:RestaurantOrder): RestaurantOrderResponse{
//    var resp :HttpResponse
//    resp = client.post(restaurantList[id]?.address + "/v2/order"){
//        setBody(Json.encodeToString(RestaurantOrder.serializer(), ord))
//        timeout {
//            requestTimeoutMillis = 100000
//        }
//            }
//    return Json.decodeFromString(RestaurantOrderResponse.serializer(), resp.body())
//
//}
