(ns q4.events
  (:require [q4.db :as db]
            [re-frame.core :refer [dispatch reg-event-db reg-event-fx reg-sub]]))

;;dispatchers

(reg-event-db
  :initialize-db
  (fn [_ _]
    db/default-db))

(reg-event-db
  :choose-game
  (fn [db [_ game]]
    (assoc db :game game :game-chosen true)))

(defn same-color?
  "Tests to see if a given space on a given board matches a given color and is not :e"
  [board space orig-color]
  (let [space-color (get-in board space)]
    (and (= space-color orig-color)
         (not= space-color :e))))

(defn new-space-quasi
  "Finds the new space for super 4. If the starting space is at the left or right
  edge, returns space on the opposite edge. If not, returns standard new space"
  [starting-space direction]
  (let [new-space (vec (map + starting-space direction))]
    (when (seq new-space)
      (cond
        (>= (second new-space) 7) (assoc new-space 1 0)
        (<= (second new-space) -1) (assoc new-space 1 6)
        :else new-space))))

(defn new-space-quantum
  "Find the new space for quantum 4. If the starting space is at any edge, returns
   the space on the opposite edge. If not, returns standard new space"
  [starting-space direction]
  (let [new-space (vec (map + starting-space direction))]
    (when (seq new-space)
      (cond
        (>= (second new-space) 7)  (assoc new-space 1 0)
        (<= (second new-space) -1) (assoc new-space 1 6)
        (>= (first new-space) 6)   (assoc new-space 0 0)
        (<= (first new-space) -1)  (assoc new-space 0 5)
        :else new-space))))

(defn four-in-a-row? [board space game]
  "Takes a board, a space, and a game-type, and returns true if that space on the
   board is the beginning or end of 4 in a row, according to the game-type"
  (let [color (get-in board space)
        get-new-space (case game
                        :quaint #(map + %1 %2)
                        :quasi new-space-quasi
                        :quantum new-space-quantum)]
    (loop [directions  [[0 1]
                        [-1 1]
                        [-1 0]
                        [-1 -1]
                        [0 -1]
                        [1 -1]
                        [1 0]
                        [1 1]]
           starting-space space
           connections 1]
      (let [new-space (get-new-space starting-space (first directions))]
        (if (seq directions)
          (if (not= connections 4)
            (if (same-color? board new-space color)
              (recur directions new-space (inc connections))
              (recur (next directions) space 1))
            true)
          false)))))

(defn winning-board [board game]
  "Returns true if a board have 4 in a row, according to the game type"
  (let [spaces (for [i (range 6)
                     j (range 7)]
                 [i j])]
    (as-> spaces s
          (map #(four-in-a-row? board % game) s)
          (set s)
          (s true))))

(reg-event-fx
  :check-board
  (fn [{:keys [db]}]
    (let [colors {:r "Red" :b "Black"}]
      (if (winning-board (:board db) (:game db))
        {:db (assoc db :alert (str ((:turn db) colors) " Wins!") :active false)}
        {:dispatch [:change-turn]}))))

(reg-event-db
  :change-turn
  (fn [db _]
    (let [old-turn (:turn db)
          new-turn (if (= old-turn :r) :b :r)]
      (assoc db :turn new-turn :alert ""))))

(defn find-empty-space [[orig-row column] board]
  "Given a space and a board, this function returns lowest empty space in the column"
  (loop [row 6]
    (if (>= row 0)
      (if (not= :e (get-in board [row column]))
        (recur (dec row))
        [row column])
      "full row")))

(reg-event-fx
  :select-column
  (fn [{:keys [db]} [_ space]]
    (let [empty-space (find-empty-space space (:board db))]
      (if (not= empty-space "full row")
        {:db (update db :board #(assoc-in % empty-space (:turn db)))
         :dispatch [:check-board]}
        {:db (assoc db :alert "This column is full, select a different one")}))))

(reg-event-db
  :hover-column
  (fn [db [_ space]]
    (assoc db :hover-cell space)))

(reg-event-fx
  :play-again
  (fn [{:keys [db]} _]
    {:dispatch [:initialize-db]}))

(reg-event-db
  :set-active-page
  (fn [db [_ page]]
    (assoc db :page page)))

(reg-event-db
  :set-docs
  (fn [db [_ docs]]
    (assoc db :docs docs)))

;;subscriptions

(reg-sub
  :page
  (fn [db _]
    (:page db)))

(reg-sub
  :docs
  (fn [db _]
    (:docs db)))

(reg-sub
  :board
  (fn [db]
    (:board db)))

(reg-sub
  :turn
  (fn [db]
    (:turn db)))

(reg-sub
  :alert
  (fn [db]
    (:alert db)))

(reg-sub
  :active
  (fn [db]
    (:active db)))

(reg-sub
  :game-chosen
  (fn [db]
    (:game-chosen db)))

(reg-sub
  :hover-cell
  (fn [db]
    (:hover-cell db)))


