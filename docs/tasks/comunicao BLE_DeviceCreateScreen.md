# Comunicacao BLE DeviceCreateScreen

- Descrição: Ajustes na view `DeviceCreateScreen`
- Status: In Progress
- Prioridade: Alta
- Data de Criação: 23/11/2025
- Responsável: AI Agent
- Revisao: Andre

## Detalhamento
Essa View esta funcionando bem, mas preciso de um ajuste. Apos o retorno da chamada que o app faz na API, se a resposta for 200: 
1. Nos recebemos um json que deve ser mapeado por `DeviceAdoptionResponse` que ja esta criada em `app/src/main/java/com/dev/deviceapp/model/device/DeviceCreateModel.kt`

2. Crie um modelo em `app/src/main/java/com/dev/deviceapp/model/device/` chamado DeviceAdoptionBoard com os fields 
    `device_name, user_uuid, device_uuid, broker_url, topic`, todos sao do tipo String

3. `DeviceAdoptionBoard` deve receber os valores do json contidos em `DeviceAdoptionResponse` 

4. Fazer uma comunicacao BLE com o device e enviar `DeviceAdoptionBoard`
    observe que em `BleConstants` que esta em `app/src/main/java/com/dev/deviceapp/ble/BleConstants.kt`.
    observe esse codigo que esta no esp32 : 
    from micropython import const
    import asyncio
    import aioble
    import bluetooth
    import ujson as json
    from config_tool import DeviceConfig

    _BLE_SERVICE_UUID = bluetooth.UUID('19b10000-e8f2-537e-4f6c-d104768a1214')
    _BLE_ADOPTION_UUID = bluetooth.UUID('19b10001-e8f2-537e-4f6c-d104768a1214')
    _BLE_RESPONSE_ADOPTION_UUID = bluetooth.UUID('19b10002-e8f2-537e-4f6c-d104768a1214')
    _BLE_DEVICE_INFO_UUID = bluetooth.UUID('19b10003-e8f2-537e-4f6c-d104768a1214')

    _ADV_INTERVAL_MS = 250_000
    _MTU_SIZE = 512  # MTU máximo
    _BLE_APPEARANCE = 0  # Generic Unknown

    # Register GATT server, the service and characteristics
    ble_service = aioble.Service(_BLE_SERVICE_UUID)

    adoption_characteristic = aioble.Characteristic(
        ble_service,
        _BLE_ADOPTION_UUID,
        read=True,
        write=True,
        notify=True,
        capture=True
    )


    adoption_response_characteristic = aioble.Characteristic(
        ble_service,
        _BLE_RESPONSE_ADOPTION_UUID,
        notify=True
    )


    device_info_characteristic = aioble.Characteristic(
        ble_service,
        _BLE_DEVICE_INFO_UUID,
        write=True,
        notify=True
    )


    # Register service(s)
    aioble.register_services(ble_service)

    # Global connection variable
    active_connection = None

    # Helper to encode the data characteristic UTF-8
    def _encode_data(data):
        return str(data).encode('utf-8')


    async def peripheral_task():
        global active_connection
        
        while True:
            try:
                mac_address = await DeviceConfig.get_mac_address()
                
                async with await aioble.advertise(
                    _ADV_INTERVAL_MS,
                    name="ESP32_" + mac_address,
                    services=[_BLE_SERVICE_UUID],
                    appearance=_BLE_APPEARANCE,
                ) as connection:
                    active_connection = connection
                    print(f"Connection from {connection.device}")
                    
                    try:
                        await connection.exchange_mtu(_MTU_SIZE)
                        actual_mtu = connection.mtu
                        print(f"MTU negociado: {actual_mtu} bytes")
                    except Exception as e:
                        actual_mtu = connection.mtu
                        print(f"MTU default: {actual_mtu} bytes, erro: {e}")
                    
                    await connection.disconnected()
                    active_connection = None
                    print("Connection disconnected")
                    
            except asyncio.CancelledError:
                break
            except Exception as e:
                print(f"Error peripheral_task: {e}")
            await asyncio.sleep_ms(1000)


    async def wait_for_adoption_write():
        buffer = b""
        while True:
            try:
                connection, data = await adoption_characteristic.written()
                print('BLE Connection: ', connection)
                
                buffer += data
                
                try:
                    payload = json.loads(buffer.decode())
                    print('BLE Data: ', payload)
                    buffer = b""
                    
                except Exception as e:
                    print(f"Error wait_for_adoption_write: parsing JSON: {e}")
                    print("Raw data:", data)
                    continue
                
                device_name = payload.get("device_name")
                user_uuid   = payload.get("user_uuid")
                device_uuid = payload.get("device_uuid")
                broker_url  = payload.get("broker_url")
                topic       = payload.get("topic")
                #wan_ssid    = payload.get("wan_ssid")
                #wan_pass    = payload.get("wan_pass")
                
                _adopted_device = await DeviceConfig.write_adopted_device(
                    device_name = device_name,
                    user_uuid = user_uuid,
                    device_uuid = device_uuid,
                    broker_url = broker_url,
                    topic = topic,
                    #wan_ssid = wan_ssid,
                    #wan_pass = wan_pass        
                ) 
                
                if connection and connection.is_connected():
                    # Send notification
                    response_message = f"{_adopted_device['code']}"
                    adoption_response_characteristic.write(response_message.encode(), send_update=True)
                    print(f"Sent response: {response_message}")
                else:
                    print("Cannot notify: connection is not active")

            except asyncio.CancelledError:
                print("Error wait_for_adoption_write: peripheral task cancelled")
            except Exception as e:
                print(f"Error wait_for_adoption_write: {e}")
            finally:
                await asyncio.sleep_ms(100)


    async def device_info_task():
        while True:

            connection = await device_info_characteristic.written()
            print(f"Ok device_info_task: requested by {connection.device}")

            config = await DeviceConfig.read_device_config()
            
            if not config:
                payload = b"{}"
            else:
                payload = json.dumps(config).encode()

            if connection.is_connected():
                try:
                    device_info_characteristic.write(payload, send_update=True)
                    await asyncio.sleep(0.5)
                except Exception as ex:
                    print(f"Error device_info_task: {ex}")
                    continue  
            else:
                print("Error device_info_task: no connection")
            
            print("Ok device_info_task: sent device info json")
            
    precisamos fazer a chamada para wait_for_adoption_write()

    5. a resposta a essa comunicao BLE vira da caracteristica `adoption_response_characteristic`, se houver resposta, entao devemos mostrar um toast longa duracao com uma menssagem de "Adopition Success" e retornar a tela anterior
    
## Duvidas
- 1. broker_url: não está em DeviceAdoptionResponse.Success. O JSON da API retorna esse campo, mas não está mapeado no modelo? Ou deve vir de outra fonte (ex.: broker do usuário)?

- 2. Mapeamento de DeviceAdoptionBoard:
    device_name ← DeviceAdoptionResponse.Success.name
    user_uuid ← DeviceAdoptionResponse.Success.userUuid
    device_uuid ← DeviceAdoptionResponse.Success.uuid
    topic ← DeviceAdoptionResponse.Success.message.topic
    broker_url ← de onde vem?
    Vem de DeviceAdoptionResponse.Success.brokerUrl

- 3. Conexão BLE: usar o mesmo BluetoothDevice que já está conectado na tela 
    (vindo do parâmetro mac)? Ou fazer uma nova conexão?
    Sim usar o mesmo

- 4. Onde fazer a comunicação BLE: no DeviceCreateViewModel ou criar um novo 
    método/repositório? Já existe writeAdoptionData no DeviceOptionsViewModel que pode ser reutilizado.
    Siga o modelo de codigo da chamada BLE que fazemos para obter as infos do Device
    

- 5. Resposta do ESP32: o código mostra que envia um código 
    (ex.: response_message = f"{_adopted_device['code']}"). Qual valor indica sucesso? Qualquer resposta não vazia já indica sucesso?
    200 status OK, qualquer coisa diferente Error

## Notas
