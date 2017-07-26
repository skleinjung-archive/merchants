import tensorflow as tf
import tensorflow.contrib.slim as slim
import numpy
import buymorecards as bmc


class Agent:
    def __init__(self):
        # These lines established the feed-forward part of the network. The agent takes a state and produces an action.
        self.state_in = tf.placeholder(shape=[None, 12], dtype=tf.float32, name="state_in")
        hidden = slim.fully_connected(self.state_in, 144, biases_initializer=None, activation_fn=tf.nn.relu)
        # hidden2 = slim.fully_connected(hidden, 3744, biases_initializer=None, activation_fn=tf.nn.relu)
        self.output = slim.fully_connected(hidden, 12, activation_fn=tf.nn.softmax, biases_initializer=None)
        self.chosen_action = tf.argmax(self.output, 1)

        self.target_output = tf.placeholder(shape=[1, 12], dtype=tf.float32)
        self.loss = tf.reduce_sum(tf.square(self.target_output - self.output))
        # self.loss = tf.nn.l2_loss(self.target_output - self.output)

        trainer = tf.train.GradientDescentOptimizer(learning_rate=0.1)
        self.updateModel = trainer.minimize(self.loss)


def get_card_color(card_index):
    if card_index < 3:
        return bmc.Color.Red
    if card_index < 6:
        return bmc.Color.Yellow
    if card_index < 9:
        return bmc.Color.Green
    return bmc.Color.Blue


def get_card_value(card_index):
    if card_index % 3 == 0:
        return 2
    if card_index % 3 == 1:
        return 3
    if card_index % 3 == 2:
        return 5


episodes_per_batch = 500
number_of_batches = 100000000

env = bmc.get_environment(bmc.Environment.TakeValidCards)

tf.reset_default_graph()
agent = Agent()
weights = tf.trainable_variables()[0]
init = tf.global_variables_initializer()
with tf.Session() as session:
    session.run(init)

    for batch in range(number_of_batches):
        turn_count = 0
        for episode in range(episodes_per_batch):
            state = env.reset()

            while True:
                turn_count += 1

                chosen_action, output = session.run([agent.chosen_action, agent.output], feed_dict={agent.state_in: [state]})
                new_state, reward, done = env.step(get_card_color(chosen_action), get_card_value(chosen_action))

                Q1 = session.run(agent.output, feed_dict={agent.state_in: [new_state]})
                maxQ1 = numpy.max(Q1)
                y = 0.99
                target_output = output
                target_output[0][chosen_action] = reward + y * maxQ1
                session.run(agent.updateModel, feed_dict={agent.state_in: [state], agent.target_output: target_output})

                state = new_state

                if done:
                    break

        print("Batch: {}, Average Turns: {}".format(batch + 1, turn_count / episodes_per_batch))
