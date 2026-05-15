# 🌿 Eco-Verse: Platform For Sustainability

A sustainability-focused digital platform that transforms everyday grocery shopping into a circular economy loop — combining **e-commerce**, **AI-powered waste classification**, and a **gamified rewards system** to reduce India's food waste crisis.

> 📌 Final Year Major Project — B.E. Computer Science (Data Science)  
> Lokmanya Tilak College of Engineering, University of Mumbai (2025–26)

---

## 📖 About The Project

India generates over **62 million tonnes of waste annually**, yet only 20–25% is scientifically processed. Eco-Verse addresses this by merging grocery shopping and household waste collection into a single platform.

When a customer orders groceries, the delivery agent simultaneously collects their household organic waste at the doorstep. The waste is then classified by an AI model and redirected to its most sustainable use — animal feed, composting, or biogas production. Users earn **EcoCoins** as rewards for every kg of waste they contribute.

---

## ✨ Features

- 🛒 **EcoGrocer Marketplace** — Browse and order fresh groceries with an eco-themed UI
- ♻️ **Dual-Logistics Framework** — Grocery delivery + organic waste pickup in a single trip
- 🤖 **AI Waste Classifier** — CNN-based model (98.08% accuracy) classifies waste by freshness
- 🪙 **EcoCoins Rewards** — Gamified system: 1 kg waste = 10 EcoCoins, redeemable for discounts
- 📊 **Impact Dashboard** — Real-time tracking of CO₂ saved and waste diverted from landfills
- 📅 **Waste Pickup Scheduling** — Choose morning, afternoon, or evening pickup slots
- 🔐 **Firebase Authentication** — Secure OTP-based login

---

## 🛠️ Tech Stack

| Layer | Technology |
|---|---|
| Frontend / Mobile | Java, Android (XML) |
| Backend | Python, Java |
| Database | Firebase Realtime Database |
| AI Model | Python, CNN (Keras Sequential API) |
| Payment | Razorpay |
| Authentication | Firebase Auth |

---

## 🤖 AI Model — Peel Identifier

The core AI module uses a **Convolutional Neural Network (CNN)** built with the Keras Sequential API to classify collected organic waste into three categories:

| Score | Condition | Directed To |
|---|---|---|
| High | Fresh | Animal Feed |
| Medium | Slightly decayed | Composting / Fertilizer |
| Low | Fully decayed | Biogas / Methane Production |

- **Accuracy:** 98.08%
- **Activation:** Softmax (multi-class classification)
- Detects: discoloration, mold, bruising, texture changes

---

## ⚙️ How It Works

1. User logs in via Firebase Authentication
2. Browses products and adds items to cart
3. At checkout, opts in for **"Eco-Collect"** (waste pickup)
4. Delivery agent delivers groceries and collects organic waste
5. Waste is scanned via the **AI Peel Identifier**
6. Waste is classified and redirected to appropriate facility
7. **EcoCoins** are credited to the user's wallet
8. User can redeem coins for grocery discounts

---

## 📊 Results

- ✅ **42.3% reduction** in negative environmental effects vs traditional delivery models
- ✅ **98.08% accuracy** in AI waste classification
- ✅ Reward rate: **1 kg organic waste = 10 EcoCoins**
- ✅ Addresses India's **78.2 million tonnes** annual food waste (UN Food Waste Index 2024)

---

## 👥 Team

| Name 
|---|---|
| Samiksha Ojha 
| Mahesh Shukla
**Department:** Computer Science and Engineering (Data Science)  
**Institute:** Lokmanya Tilak College of Engineering, Navi Mumbai

---

## 📚 References

- UN Environment Programme — Food Waste Index Report 2024
- IEEE ITCC — A Circular Economy: Moving from Supply and Value Chains to Networks
- W. M. Lim et al. — Gamification for Sustainable Consumption (Business Strategy and the Environment, 2025)
- IEEE Xplore — A CNN-based Model to Classify Fresh and Damaged Fruit (2024)

---

## 📄 License

This project was developed for academic purposes at Lokmanya Tilak College of Engineering.
