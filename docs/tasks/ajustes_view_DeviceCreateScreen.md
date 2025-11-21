# Ajustes view DeviceCreateScreen

- Descrição: Ajustes na view `DeviceCreateScreen`
- Status: In Progress
- Prioridade: Alta
- Data de Criação: 21/11/2025
- Responsável: Agent
- Revisao: Andre

## Detalhamento
- Ao acessar a pagina garantir que o usuario logado tenha um token. Tome como exemplo o codigo que esta em `ProfileScreen` package `com.dev.deviceapp.view.profile` esse oe o ponto:
    val tokenInfo = viewModel.tokenInfo
        if (tokenInfo == null) {
            Log.e("ProfileScreen", "Token is null")
            LaunchedEffect(Unit) { onLogout() }
            return
        }
    val uiStateUserGet by userGetViewModel.state.collectAsState()
    LaunchedEffect(Unit) {
        userGetViewModel.getUser(
            UserGetRequest(uuid = tokenInfo.uuid)
        )
    }

    LaunchedEffect(uiStateUserGet) {
        when (uiStateUserGet) {
            is UserGetUiState.Error -> {
                val message = (uiStateUserGet as UserGetUiState.Error).message
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            }
            else -> {}
        }
    }

- Veja que tokenInfo tem tokenInfo.uuid esse valor deve ser o valor do field User UUID que nao sera um imput e sim um valor listado, coloqueo abaixo do field Adoption Status

- Garantir que apos o recebimento das informacoes vindas da comunicao BLE, pelo menos um desses valores nao seja null: 
    `"Sensor Type"`,
    `"Actuator Type"`,. 
    Se ambos forem nulos: 
     - Colocar um log de erro como uma informacao "Inconsistente device data sensor_type and actuator_type are null".
     - Mostrar um toast longa duracao como uma informacao "Inconsistente device data".
     - Retornar a tela anterior

- Garanta a obrigatoriedade dos fileds vindos das informacoes vindas da comunicao BLE:
    `MAC Address`
    `Board Type`
    `Sensor Type` ou `"Actuator Type"`
    `Adopted Status`
    Se algum desses fields forem null ou "":
     - Colocar um log de erro como uma informacao "Inconsistente device e quais os fields obrigatorios nao foram enviados".
     - Mostrar um toast longa duracao como uma informacao "Inconsistente device data".
     - Retornar a tela anterior

- Garanta que o field `Adopted Status` seja igual a `Unadopted`
    Se for diferente:
     - Colocar um log de erro como uma informacao "Inconsistente device e o valor recebido em `Adopted Status`".
     - Mostrar um toast longa duracao como uma informacao "Inconsistente device data".
     - Retornar a tela anterior

- Os valores vindos da comunicao BLE se forem do tipo null ou "", seram preenchidos com `---`

- Veja que na requisicao para a API de criacao do device em "message": {
        "qos": 1,
        "retained": true,
        "publisher": true,
        "subscriber": true,
        "command_start": 1,
        "command_end": 0
    }, voce deve criar esses imputs para que o usuario possa preencher, coloque-os logo abaixo do field Broker URL

    `qos` valores possiveis no imput list: 0,1,2,3. Deixar 1 selecionado como padrao
    `retained` valores possiveis no imput list: true ou false. Deixar true selecionado como padrao.
    `publisher`: valores possiveis no imput list: true ou false. Deixar true selecionado como padrao.
    `subscriber`: valores possiveis no imput list: true ou false. Deixar true selecionado como padrao.
    `command_start`: Deixar 1 como padrao.
    `command_end`: Deixar 1 como padrao.

- Visualmente e necessario retirar a informacao Device UUID mostrada na view `DeviceCreateScreen`, o status para um dispositivo ainda nao adotado sempre sera AdoptionStatus -> Unadopted.

- O field `User UUID` deve ser um campo nao editavel e deve ser preenchido com o tokenInfo.uuid

## Notas
- Exemplo do payload json esperado pela API para criacao do device:
  method: POST
  url: 127.0.0.1:8081/device
  body:
    {
    "name": "deviceTeste4",
    "device_type_str": "Sensor",
    "border_type_str": "ESP32",
    "sensor_type": "DHT11",
    "actuator_type": null,
    "adopted_status": "adopted",
    "mac_address": "3A:5F:9C:12:8E:80",
    "message": {
        "qos": 1,
        "retained": true,
        "publisher": true,
        "subscriber": true,
        "command_start": 1,
        "command_end": 0
    },
    "scale":[
        [
            "temperature",  "C"
        ], 
        [
            "humidty", "%"
        ]
    ]
  }

- Na API ha uma struct Rust `DeviceCreateRequest` responsavel por validar a requisicao de criacao do Device:
pub struct DeviceCreateRequest{
    name: String,
    device_type_str: String,
    border_type_str: String,
    sensor_type: Option<String>,
    actuator_type: Option<String>,
    adopted_status: String,
    mac_address: String,
    message: DeviceMessageCreateRequest,
    scale: Option<Vec<(String, String)>>,
}

pub struct DeviceMessageCreateRequest {
    qos: i32,
    retained: bool,
    publisher: Option<bool>,
    subscriber: Option<bool>,
    command_start: Option<i32>,
    command_end: Option<i32>,
    command_last: Option<i32>,
}