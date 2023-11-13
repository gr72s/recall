"use strict";
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
var path_1 = __importDefault(require("path"));
var mysql_1 = __importDefault(require("mysql"));
var express_1 = __importDefault(require("express"));
var app = (0, express_1.default)();
app.use(express_1.default.json());
app.use("/", express_1.default.static(path_1.default.join(__dirname, "../../client/dist")));
// Enable CORS so that we can call the API even from anywhere.
app.use(function (inRequest, inResponse, inNext) {
    inResponse.header("Access-Control-Allow-Origin", "*");
    inResponse.header("Access-Control-Allow-Methods", "GET,POST,DELETE,OPTIONS");
    inResponse.header("Access-Control-Allow-Headers", "Origin,X-Requested-With,Content-Type,Accept");
    inNext();
});
console.log(process.env.MYSQL_HOST);
console.log(process.env.MYSQL_PORT);
console.log(process.env.MYSQL_USER);
console.log(process.env.MYSQL_PASSWORD);
var my = mysql_1.default.createConnection({
    host: process.env.MYSQL_HOST,
    port: parseInt(process.env.MYSQL_PORT),
    user: process.env.MYSQL_USER,
    password: process.env.MYSQL_PASSWORD,
    database: "recall",
    timeout: 10000,
});
my.connect();
my.query("SELECT 1 + 1 AS solution", function (error, results, fields) {
    if (error)
        throw error;
    console.log("The solution is: ", results[0].solution);
});
my.end();
//# sourceMappingURL=main.js.map