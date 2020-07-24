from EmailHandler import sendEmail
import boto3

TABLE_NAME = 'Fortunes'


def lambda_handler(event, context):
    # if using aws, please comment out endpoint_url
    ddb = boto3.resource('dynamodb',
                         endpoint_url='http://host.docker.internal:8000')
    table = None
    if isTableExist(ddb, TABLE_NAME):
        print('Table already exists')
        table = ddb.Table(TABLE_NAME)
    else:
        table = createTable(ddb)
        insertData(table)

    # get data
    items = scanItems(table)
    contentList = []
    for item in items:
        contentList.append(item['FortuneId'] + '. ' + item['FortuneContent'])

    # send email
    content = '\n'.join(contentList)
    sendEmail(content)


def isTableExist(ddb, tableName):
    tableNames = [table.name for table in ddb.tables.all()]
    if tableName in tableNames:
        return True
    return False


def createTable(ddb):
    # Create table
    table = ddb.create_table(TableName=TABLE_NAME,
                             AttributeDefinitions=[
                                 {
                                     'AttributeName': 'FortuneId',
                                     'AttributeType': 'S'
                                 }
                             ],
                             KeySchema=[
                                 {
                                     'AttributeName': 'FortuneId',
                                     'KeyType': 'HASH'
                                 }
                             ],
                             ProvisionedThroughput={
                                 'ReadCapacityUnits': 1,
                                 'WriteCapacityUnits': 1,
                             }
                             )
    # Wait until the table exists.
    table.meta.client.get_waiter('table_exists').wait(TableName=TABLE_NAME)
    print('Successfully created table')
    return table


def insertData(table):
    with table.batch_writer() as batch:
        batch.put_item(
            Item={'FortuneId': '1',
                  'FortuneContent': 'An exciting adventure awaits you.'}
        )
        batch.put_item(
            Item={'FortuneId': '2', 'FortuneContent': 'Work with what you have.'}
        )
        batch.put_item(
            Item={'FortuneId': '3',
                  'FortuneContent': 'Move quickly. Now is the time to make progress.'}
        )
    print('Successfully insert item')


def scanItems(table):
    # Scan table
    scanResponse = table.scan(TableName=TABLE_NAME)
    items = scanResponse['Items']
    return items
