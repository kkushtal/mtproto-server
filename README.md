// Regular Client request

[Server] Listening on /127.0.0.1:4556
[Server] Socket connection [Waiting]
[Client] Connected: /127.0.0.1:4556
[Server] Socket connection [Connected]
[Client] ****************************************
[Client] Send [ReqPQ = Request(Headers(41679,41650,0),Route(1615239032),Auth(Nonce(16240),Nonce(0)),ReqPQ())]
[Server] Receive [ReqPQ = Request(Headers(41679,41650,0),Route(1615239032),Auth(Nonce(16240),Nonce(0)),ReqPQ())]
[Server] Send [ResPQ = Request(Headers(41679,19145,59160),Route(85337187),Auth(Nonce(16240),Nonce(99268)),ResPQ(PQ(-114,ZZdUsrNiPI,7oNBE7zJuF,ByteVector(3 bytes, 0x5fc86c)),ByteVector(4 bytes, 0xa02bbf39),14991,ByteVector(8 bytes, 0x3947161273dbf9fd)))]
[Client] Receive [ResPQ = Request(Headers(41679,19145,59160),Route(85337187),Auth(Nonce(16240),Nonce(99268)),ResPQ(PQ(-114,ZZdUsrNiPI,7oNBE7zJuF,ByteVector(3 bytes, 0x5fc86c)),ByteVector(4 bytes, 0xa02bbf39),14991,ByteVector(8 bytes, 0x3947161273dbf9fd)))]
[Client] ****************************************
[Client] ****************************************
[Client] Send [ReqDH = Request(Headers(41679,44810,5854),Route(-686627650),Auth(Nonce(16240),Nonce(99268)),ReqDH(ZZdUsrNiPI,7oNBE7zJuF,4127291852017367549,tulM4NBCMM))]
[Server] Receive [ReqDH = Request(Headers(41679,44810,5854),Route(-686627650),Auth(Nonce(16240),Nonce(99268)),ReqDH(ZZdUsrNiPI,7oNBE7zJuF,4127291852017367549,tulM4NBCMM))]
[Server] Send [ResDH_OK = Request(Headers(41679,32711,92332),Route(-790100132),Auth(Nonce(16240),Nonce(99268)),ResDH_OK(GhMgBGBJh5))]
[Client] Receive [ResDH_OK = Request(Headers(41679,32711,92332),Route(-790100132),Auth(Nonce(16240),Nonce(99268)),ResDH_OK(GhMgBGBJh5))]
[Client] ****************************************
[Client] Disconnected: /127.0.0.1:4556
[Server] Socket connection [Disconnected]
[Server] Socket connection [Waiting]

// Client requests with invalid Auth

[Client] Connected: /127.0.0.1:4556
[Server] Socket connection [Connected]
[Client] ****************************************
[Client] Send [ReqPQ = Request(Headers(11813,88694,0),Route(1615239032),Auth(Nonce(7598),Nonce(0)),ReqPQ())]
[Server] Receive [ReqPQ = Request(Headers(11813,88694,0),Route(1615239032),Auth(Nonce(7598),Nonce(0)),ReqPQ())]
[Server] Send [ResPQ = Request(Headers(11813,26205,94459),Route(85337187),Auth(Nonce(7598),Nonce(91134)),ResPQ(PQ(36,PsPRBDIGbD,6naxOewBoe,ByteVector(3 bytes, 0x3b7602)),ByteVector(4 bytes, 0x818025af),51091,ByteVector(8 bytes, 0x69d9aaeaefa3ec26)))]
[Client] Receive [ResPQ = Request(Headers(11813,26205,94459),Route(85337187),Auth(Nonce(7598),Nonce(91134)),ResPQ(PQ(36,PsPRBDIGbD,6naxOewBoe,ByteVector(3 bytes, 0x3b7602)),ByteVector(4 bytes, 0x818025af),51091,ByteVector(8 bytes, 0x69d9aaeaefa3ec26)))]
[Client] ****************************************
[Client] ****************************************
[Client] Send [ReqDH = Request(Headers(11813,61852,63241),Route(-686627650),Auth(Nonce(0),Nonce(0)),ReqDH(PsPRBDIGbD,6naxOewBoe,7627315369948212262,trLeLRas0z))]
[Server] Execution failed with: java.lang.Throwable: Authentication failed
[Client] Execution failed with: java.lang.Exception: Error occurred: cannot acquire 64 bits from a vector that contains 0 bits
[Server] Socket connection [Disconnected]
[Server] Socket connection [Waiting]


.....