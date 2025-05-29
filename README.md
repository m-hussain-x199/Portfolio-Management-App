# Animated Portfolio Manager

A modern, animated Java Swing application for managing your stock and crypto portfolio, inspired by professional trading dashboards.  
This app features a visually appealing interface with animated backgrounds, gradient effects, custom-styled controls, and smooth transitions.

---

## Features

- **Animated Gradient Background:** Dynamic sine waves and glowing dots for a modern look.
- **Custom UI Components:** Gradient buttons, rounded cards, and styled input fields.
- **User Authentication:** Register and login with secure password confirmation.
- **Portfolio Management:**  
  - View your holdings in a clean, card-based layout.
  - Buy and sell stocks or crypto assets with instant feedback.
  - Undo your last transaction.
- **Transaction History:**  
  - View all past buy/sell actions.
  - Easy navigation between dashboard, portfolio, trading, and history.
- **Responsive Design:**  
  - Scales well on different window sizes.
  - All controls are keyboard and mouse friendly.
- **Persistent Storage:**  
  - User data is saved to `portfolio_data.ser` and loaded automatically.

---


## How to Run

1. **Requirements:**  
   - Java 8 or newer

2. **Compile:**
   ```sh
   javac PortfolioAppOKXAnimated.java
   ```

3. **Run:**
   ```sh
   java PortfolioAppOKXAnimated
   ```

4. **Data:**  
   - User data is stored in `portfolio_data.ser` in the same directory.

---

## Usage

- **Sign In:**  
  Enter your username and password. If you don't have an account, click "Create Account".
- **Register:**  
  Choose a username and password, confirm your password, and click "Register".
- **Dashboard:**  
  Navigate to view your portfolio, buy/sell assets, view transaction history, or undo your last trade.
- **Portfolio:**  
  See all your holdings. If empty, buy some stocks or crypto to get started.
- **Buy/Sell:**  
  Enter the ticker, quantity, and price. The app will update your portfolio and transaction history.
- **History:**  
  Review all your past transactions.

---

## Customization

- **Colors & Fonts:**  
  Easily change the color scheme and fonts at the top of the source file.
- **UI/UX:**  
  All UI components are modular and can be extended for more features (charts, analytics, etc).

---

## Credits

- Inspired by OKX, TradingView, and modern fintech dashboards.
- Developed with pure Java Swing and custom painting (no external libraries required).

---

## License

This project is provided for educational and personal use.  
Feel free to modify and extend it for your own portfolio management needs.

---

## Author

This Project is created by Muhammad Hussain. 

LinkedIn: www.linkedin.com/in/where-is-hussain
