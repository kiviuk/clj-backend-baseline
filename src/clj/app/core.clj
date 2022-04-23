(ns app.core
  (:gen-class)
  (:require [ring.adapter.jetty :as jetty]
            [ring.middleware.defaults :as ring-defaults]
            [reitit.ring :as ring]))


(defonce server (atom nil))


(defn handler [req]
  {:status 200, :body "OK200"})


(def router
  (ring/router
   [["/api" {:get handler}]
    ["/test" {:get handler}]]))


(def app (ring/ring-handler router))


(defn start-jetty! []
  (reset!
   server
   (jetty/run-jetty (ring-defaults/wrap-defaults
                     #'app
                     ring-defaults/site-defaults)
                    {:join? false
                     :port 4000})))
 

(defn stop-jetty! []
  (.stop @server)
  (reset! server nil))


(defn -main [& args]
  (println "Server started!")
  (start-jetty!))


(defn restart-jetty! []
  (stop-jetty!)
  (-main))


(comment
  (-main))


(comment
  (stop-jetty!))


(comment
  (restart-jetty!)
  (app
   {:request-method :get, :uri "/api"}))

