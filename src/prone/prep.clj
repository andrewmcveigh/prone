(ns prone.prep
  (:require [clojure.java.io :as io]))

(defn- load-source [frame]
  (assoc frame :source (if-not (:class-path-url frame)
                         "(unknown source file)"
                         (if-not (io/resource (:class-path-url frame))
                           "(could not locate source file on class path)"
                           (slurp (io/resource (:class-path-url frame)))))))

(defn- set-application-frame [application-name frame]
  (if (and (:package frame)
           (.startsWith (:package frame) (str application-name ".")))
    (assoc frame :application? true)
    frame))

(defn prep [error request application-name]
  {:error (-> error
              (update-in [:frames]
                         #(->> %
                               (map-indexed (fn [idx f] (assoc f :id idx)))
                               (map (partial set-application-frame application-name))
                               (mapv load-source)))
              (update-in [:frames 0] assoc :selected? true))
   :request {:uri (:uri request)}
   :frame-filter :application})

(comment (defn get-application-name []
  (second (edn/read-string (slurp "project.clj")))))